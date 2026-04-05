package com.example.tictactoe.domain.service;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    public boolean register(String login, String password);
    Optional<UUID> authorize(String login, String password);
}
