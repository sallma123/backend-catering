package com.catering.backend.controller;

import com.catering.backend.model.PasswordResetToken;
import com.catering.backend.model.User;
import com.catering.backend.repository.PasswordResetTokenRepository;
import com.catering.backend.repository.UserRepository;
import com.catering.backend.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.Optional;

// Classe représentant la requête de login
class LoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔐 Connexion sécurisée
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData) {
        Optional<User> user = userRepository.findByEmail(loginData.getEmail());

        if (user.isPresent() && passwordEncoder.matches(loginData.getPassword(), user.get().getPassword())) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Email ou mot de passe incorrect"));
        }
    }

    // 🆕 Enregistrement sécurisé
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        Optional<User> existingUser = userRepository.findByEmail(newUser.getEmail());

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "Cet email est déjà utilisé"));
        }

        // Hasher le mot de passe avant de l’enregistrer
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("message", "Utilisateur créé avec succès"));
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String email,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        User user = userOptional.get();

        // ✅ Vérifie le mot de passe actuel avec hash
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Ancien mot de passe incorrect");
        }

        // ✅ Encode le nouveau mot de passe avant de le sauvegarder
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe modifié avec succès");
    }

    @Transactional
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        // Supprimer anciens tokens
        tokenRepository.deleteByEmail(email);

        // Générer un token
        String token = java.util.UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(resetToken);

        // Lien HTTP cliquable
        String linkHttp = "http://192.168.1.15:8080/api/auth/open-reset?email="
                + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8)
                + "&token=" + token;

        String message = "<p>Cliquez sur le lien suivant pour réinitialiser votre mot de passe :</p>"
                + "<a href=\"" + linkHttp + "\">Réinitialiser le mot de passe</a>";

        // Envoi email en HTML
        emailService.sendEmail(email, "Réinitialisation du mot de passe", message);

        return ResponseEntity.ok("Email envoyé");
    }



    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty() || !tokenOpt.get().getEmail().equals(email) || tokenOpt.get().isExpired()) {
            return ResponseEntity.badRequest().body("Token invalide ou expiré");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Supprimer le token après usage
        tokenRepository.delete(tokenOpt.get());

        return ResponseEntity.ok("Mot de passe réinitialisé avec succès");
    }
    @GetMapping("/open-reset")
    public ResponseEntity<Void> openReset(@RequestParam String email, @RequestParam String token) {
        // Encodage pour éviter les problèmes d'espaces, caractères spéciaux
        String encodedEmail = java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);

        // Lien deep link qui sera ouvert par l'appli
        String deepLink = "cateringapp://reset_password/" + encodedEmail + "/" + token;

        // Redirection HTTP 302 vers le deep link
        return ResponseEntity.status(302)
                .header("Location", deepLink)
                .build();
    }

}
