package com.catering.backend.controller;

import com.catering.backend.model.User;
import com.catering.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public User login(@RequestBody User loginData) {
        Optional<User> user = userRepository.findByEmailAndPassword(loginData.getEmail(), loginData.getPassword());
        return user.orElse(null); // Android vérifie si c’est null ou pas
    }
}
