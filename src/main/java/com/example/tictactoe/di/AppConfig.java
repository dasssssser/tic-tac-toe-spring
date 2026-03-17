package com.example.tictactoe.di;

import com.example.tictactoe.datasource.model.Storage;
import com.example.tictactoe.datasource.repository.GameRepositoriImpl;
import com.example.tictactoe.datasource.repository.GameRepository;
import com.example.tictactoe.datasource.mapper.GameDataMapper;
import com.example.tictactoe.domain.service.Service;
import com.example.tictactoe.domain.service.ServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AppConfig {

    @Bean
    public Map<UUID, Storage> gameStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public GameRepository gameRepository(Map<UUID, Storage> gameStorage) {
        return new GameRepositoriImpl(gameStorage);
    }

    @Bean
    public Service gameService(GameRepository gameRepository, GameDataMapper gameDataMapper) {

        return new ServiceImpl(gameRepository, gameDataMapper);
    }
}