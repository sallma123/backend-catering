package com.catering.backend.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private Session session;

    @Bean
    public DataSource dataSource() {
        try {
            int localPort = 3307;
            String sshHost = "148.251.5.11";
            int sshPort = 22;
            String sshUser = "innovat1";
            String sshPassword = "S@mia2020";

            String remoteHost = "148.251.5.11";
            int remotePort = 3306;

            // --- Ouverture du tunnel SSH ---
            JSch jsch = new JSch();
            session = jsch.getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPassword);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            System.out.println("🔌 Connexion SSH en cours...");
            session.connect();
            session.setPortForwardingL(localPort, remoteHost, remotePort);

            System.out.println("✅ Tunnel SSH ouvert : localhost:" + localPort + " → " + remoteHost + ":" + remotePort);

            // --- Création du DataSource ---
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl("jdbc:mysql://localhost:" + localPort + "/innovat1_catering?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            ds.setUsername("innovat1_salma");
            ds.setPassword("Salma2025!");
            ds.setDriverClassName("com.mysql.cj.jdbc.Driver");

            return ds;
        } catch (Exception e) {
            throw new RuntimeException("❌ Impossible d'ouvrir le tunnel SSH ou de créer le DataSource", e);
        }
    }

    @PreDestroy
    public void closeTunnel() {
        if (session != null && session.isConnected()) {
            System.out.println("🔒 Fermeture du tunnel SSH...");
            session.disconnect();
        }
    }
}
