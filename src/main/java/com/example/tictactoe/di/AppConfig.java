package com.example.tictactoe.di;

import com.example.tictactoe.datasource.repository.GameRepository;
import com.example.tictactoe.datasource.mapper.GameDataMapper;
import com.example.tictactoe.domain.service.Service;
import com.example.tictactoe.domain.service.ServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Service gameService(GameRepository gameRepository, GameDataMapper gameDataMapper) {
        return new ServiceImpl(gameRepository, gameDataMapper);
    }
}