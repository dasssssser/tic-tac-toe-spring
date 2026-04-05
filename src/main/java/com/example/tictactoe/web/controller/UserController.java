package com.example.tictactoe.web.controller;

import com.example.tictactoe.datasource.entity.UserEntity;
import com.example.tictactoe.datasource.repository.UserRepository;
import com.example.tictactoe.web.model.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable UUID userId) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            return ResponseEntity.ok(Map.of(
                    "id", user.getId().toString(),  // ← toString() для UUID
                    "login", user.getLogin()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("User not found", 404));
        }
    }
}