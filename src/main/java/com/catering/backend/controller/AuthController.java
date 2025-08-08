package com.catering.backend.controller;

import com.catering.backend.model.User;
import com.catering.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

}
