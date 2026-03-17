package com.example.tictactoe.web.controller;

import com.example.tictactoe.domain.model.GameModel;
import com.example.tictactoe.domain.service.Service;
import com.example.tictactoe.datasource.repository.GameRepository;
import com.example.tictactoe.datasource.mapper.GameDataMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.tictactoe.web.mapper.GameWebMapper;
import com.example.tictactoe.web.model.GameWebDto;
import com.example.tictactoe.web.model.ErrorResponse;

import java.util.UUID;

@RestController
@RequestMapping("/game")
public class GameController {
    private final Service gameService;
    private final GameRepository gameRepository;
    private final GameDataMapper dataMapper;
    private final GameWebMapper webMapper;

    public GameController(Service gameService, GameRepository gameRepository,
                          GameDataMapper dataMapper, GameWebMapper webMapper) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
        this.dataMapper = dataMapper;
        this.webMapper = webMapper;
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<?> makeMove(@PathVariable UUID gameId, @RequestBody GameWebDto playerMove) {
        try {
            // получаем сохраненную игру из репозитория
            var savedStorage = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("game not found" + gameId));

            //конвертируем хран в GameModel для проверки
            GameModel savedGame = dataMapper.toGameModel(savedStorage);

            //преобразуем DTO в модель
            GameModel playerGame = webMapper.toDomain(playerMove, gameId);

            //проверяем корректность значений в DTO
            int[][] board = playerGame.getField().getField();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int val = board[i][j];
                    if (val != 0 && val != 1 && val != 2) {
                        return ResponseEntity.badRequest()
                                .body(new ErrorResponse("incorrect value in the cell" + val, 400));
                    }
                }
            }

            //проверка что игрок сходил
            if (!gameService.validation(savedGame.getField(), playerGame.getField())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("you need to place a 1 in the empty cell", 400));
            }

            // 6. Обрабатываем ход
            GameModel result = gameService.processMove(playerGame);

            // 7. Преобразуем результат в DTO
            GameWebDto response = webMapper.toDto(result);

            // 8. Добавляем статус игры
            int winner = gameService.getWinner(result.getField());
            if (winner == 1) response.setStatus("PLAYER_WON");
            else if (winner == 2) response.setStatus("COMPUTER_WON");
            else if (gameService.draw(result.getField())) response.setStatus("DRAW");
            else response.setStatus("IN_PROGRESS");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("incorrect move " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("error" + e.getMessage(), 404));
        }
    }

    @PostMapping
    public ResponseEntity<UUID> createGame() {
        try {
            UUID gameId = gameService.createNewGame();
            return ResponseEntity.status(201).body(gameId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable UUID gameId) {
        try {
            var storage = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("game not found" + gameId));

            GameModel game = dataMapper.toGameModel(storage);
            GameWebDto response = webMapper.toDto(game);

            int winner = gameService.getWinner(game.getField());
            if (winner == 1) response.setStatus("PLAYER_WON");
            else if (winner == 2) response.setStatus("COMPUTER_WON");
            else if (gameService.draw(game.getField())) response.setStatus("DRAW");
            else response.setStatus("IN_PROGRESS");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("game not found" + gameId, 404));
        }
    }
}