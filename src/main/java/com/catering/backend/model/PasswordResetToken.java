package com.catering.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String token;
    private LocalDateTime expiryDate;

    // ✅ Getter & Setter pour id
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    // ✅ Getter & Setter pour email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // ✅ Getter & Setter pour token
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    // ✅ Getter & Setter pour expiryDate
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    // ✅ Méthode utilitaire pour vérifier l'expiration
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
