package com.example.tictactoe.datasource.repository;

import com.example.tictactoe.datasource.model.Storage;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository {
    void save(Storage game);
    Optional<Storage> findById(UUID id);
}