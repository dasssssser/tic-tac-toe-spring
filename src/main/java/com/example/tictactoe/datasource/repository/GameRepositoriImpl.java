package com.example.tictactoe.datasource.repository;

import com.example.tictactoe.datasource.model.Storage;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GameRepositoriImpl implements GameRepository {

    private final Map<UUID, Storage> storage;

    public GameRepositoriImpl(Map<UUID, Storage> storage) {
        this.storage = storage;
    }

    @Override
    public void save(Storage game) {
        UUID id = game.getId();
        if (id == null) {
            throw new IllegalArgumentException("Cannot save Storage with null ID");
        }
        storage.put(id, game);
    }

    @Override
    public Optional<Storage> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }
}