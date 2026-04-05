package com.example.tictactoe.web.controller;

import com.example.tictactoe.domain.service.UserService;
import com.example.tictactoe.web.model.SignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignUpRequest request) {
        String login = request.getLogin();
        String password = request.getPassword();

        if (userService.register(login, password)) {
            return ResponseEntity.ok("User registered");
        } else {
            return ResponseEntity.badRequest().body("User already exists");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("Auth header: " + authHeader);

        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.status(401).body("Missing Authorization header");
        }

        if (!authHeader.startsWith("Basic ")) {
            return ResponseEntity.status(401).body("Invalid Authorization format");
        }

        try {
            String base64 = authHeader.substring("Basic ".length());
            String decoded = new String(Base64.getDecoder().decode(base64));
            String[] parts = decoded.split(":", 2);

            if (parts.length != 2) {
                return ResponseEntity.status(401).body("Invalid credentials format");
            }

            String login = parts[0];
            String password = parts[1];

            Optional<UUID> userId = userService.authorize(login, password);

            if (userId.isPresent()) {
                return ResponseEntity.ok(userId.get().toString());
            } else {
                return ResponseEntity.status(401).body("Invalid login or password");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authentication failed");
        }
    }
}