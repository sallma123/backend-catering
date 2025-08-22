package com.catering.backend.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshTunnelTest {
    public static void main(String[] args) {
        String sshUser = "innovat1";              // ton utilisateur SSH
        String sshHost = "148.251.5.11";          // IP de ton serveur
        int sshPort = 22;                         // port SSH (par défaut 22)
        String sshPassword = "S@mia2020";       // ton mot de passe SSH

        int localPort = 3307;                     // port local de ton PC (on choisit 3307 pour éviter conflit avec 3306)
        String remoteHost = "127.0.0.1";          // MySQL tourne sur localhost côté serveur
        int remotePort = 3306;                    // port MySQL du serveur

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPassword);

            // éviter les erreurs de clé SSH
            session.setConfig("StrictHostKeyChecking", "no");

            System.out.println("🔌 Connexion SSH en cours...");
            session.connect();
            System.out.println("✅ Connexion SSH réussie !");

            // création du tunnel : localhost:3307 → serveur:3306
            session.setPortForwardingL(localPort, remoteHost, remotePort);
            System.out.println("✅ Tunnel ouvert : localhost:" + localPort + " → " + remoteHost + ":" + remotePort);

            // garder la connexion ouverte (ici 60 secondes)
            Thread.sleep(60000);

            session.disconnect();
            System.out.println("🚪 Tunnel fermé.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
