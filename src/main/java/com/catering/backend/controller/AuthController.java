package com.catering.backend.controller;

import com.catering.backend.model.User;
import com.catering.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
        Optional<User> user = userRepository.findByEmailAndPassword(
                loginData.getEmail(), loginData.getPassword());

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            // Retourne une erreur HTTP 401 avec un message JSON
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Email ou mot de passe incorrect"));
        }
    }
}
