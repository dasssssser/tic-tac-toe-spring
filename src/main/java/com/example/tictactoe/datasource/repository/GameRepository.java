package com.example.tictactoe.datasource.repository;

import com.example.tictactoe.datasource.entity.GameEntity;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface GameRepository extends CrudRepository<GameEntity, UUID> {
}