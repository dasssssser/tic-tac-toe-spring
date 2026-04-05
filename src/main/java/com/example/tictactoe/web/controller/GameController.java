package com.example.tictactoe.web.controller;

import com.example.tictactoe.datasource.entity.GameEntity;
import com.example.tictactoe.datasource.entity.UserEntity;
import com.example.tictactoe.domain.model.GameField;
import com.example.tictactoe.domain.model.GameModel;
import com.example.tictactoe.domain.service.Service;
import com.example.tictactoe.datasource.repository.GameRepository;
import com.example.tictactoe.datasource.repository.UserRepository; // ← ДОБАВЛЕНО
import com.example.tictactoe.datasource.mapper.GameDataMapper;
import com.example.tictactoe.web.model.MoveRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.tictactoe.web.mapper.GameWebMapper;
import com.example.tictactoe.web.model.GameWebDto;
import com.example.tictactoe.web.model.ErrorResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/game")
public class GameController {
    private final Service gameService;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GameDataMapper dataMapper;
    private final GameWebMapper webMapper;

    public GameController(Service gameService,
                          GameRepository gameRepository,
                          UserRepository userRepository,
                          GameDataMapper dataMapper,
                          GameWebMapper webMapper) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.dataMapper = dataMapper;
        this.webMapper = webMapper;
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<?> makeMove(@PathVariable UUID gameId,
                                      @RequestBody MoveRequest move,
                                      @RequestHeader("X-Player-Id") UUID playerUuid) {
        try {
            // Получаем сохранённую игру из БД
            GameEntity savedEntity = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

            // Конвертируем GameEntity в GameModel
            GameModel savedGame = dataMapper.toModel(savedEntity);

            // СОЗДАЕМ НОВОЕ ПОЛЕ на основе текущего состояния
            GameField updatedField = new GameField(savedGame.getField().getField());

            // Применяем ход
            int expectedSymbol = savedEntity.getPlayer1Id().equals(playerUuid) ? 1 : 2;
            updatedField.setValue(move.getRow(), move.getCol(), expectedSymbol);

            // Создаем GameModel с обновленным полем
            GameModel playerGame = new GameModel(gameId, updatedField);
            playerGame.setPlayer1Id(savedEntity.getPlayer1Id());
            playerGame.setPlayer2Id(savedEntity.getPlayer2Id());
            playerGame.setCurrentTurnId(savedEntity.getCurrentTurnId());
            playerGame.setGameMode(savedEntity.getGameMode());

            // Проверяем корректность хода
            if (move.getRow() < 0 || move.getRow() > 2 || move.getCol() < 0 || move.getCol() > 2) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid cell coordinates", 400));
            }

            // Проверка, что клетка пустая
            if (!savedGame.getField().isCellEmpty(move.getRow(), move.getCol())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Cell is already occupied", 400));
            }

            // Проверка, чей сейчас ход
            if (!savedEntity.getCurrentTurnId().equals(playerUuid)) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Not your turn", 400));
            }

            // Валидация хода (упрощенная, так как мы уже все проверили)
            if (!gameService.validation(savedEntity.getBoard(), playerGame.getField(), expectedSymbol)) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid move", 400));
            }

            // Обрабатываем ход
            GameModel result = gameService.processMove(playerGame, playerUuid);

            // Преобразуем результат в DTO
            GameWebDto response = webMapper.toDto(result);

            // Заполняем поля DTO
            response.setPlayer1Id(result.getPlayer1Id());
            response.setPlayer2Id(result.getPlayer2Id());
            response.setCurrentTurnId(result.getCurrentTurnId());
            response.setGameMode(result.getGameMode());
            response.setWinnerId(result.getWinnerId());

            // Устанавливаем статус
            String dbStatus = gameRepository.findById(gameId).get().getStatus();
            response.setStatus(dbStatus);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Incorrect move: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("Error: " + e.getMessage(), 404));
        }
    }

    @PostMapping
    public ResponseEntity<?> createGame(@RequestParam(required = false) String mode,
                                           @RequestHeader("X-Player-Id") UUID playerUuid) {
        try {
            String gameMode = (mode == null || mode.isEmpty()) ? "PVE" : mode;

            UUID gameId = gameService.createNewGame(gameMode, playerUuid);
            return ResponseEntity.status(201).body(gameId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable UUID gameId) {
        try {
            GameEntity entity = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

            GameModel game = dataMapper.toModel(entity);
            GameWebDto response = webMapper.toDto(game);
            response.setPlayer1Id(game.getPlayer1Id());
            response.setPlayer2Id(game.getPlayer2Id());
            response.setCurrentTurnId(game.getCurrentTurnId());
            response.setGameMode(game.getGameMode());
            response.setWinnerId(game.getWinnerId());

            int winner = gameService.getWinner(game.getField());
            String dbStatus = entity.getStatus();

            if ("WIN".equals(dbStatus)) {
                response.setStatus("WIN");
            } else if ("DRAW".equals(dbStatus)) {
                response.setStatus("DRAW");
            } else if ("WAITING".equals(dbStatus)) {
                response.setStatus("WAITING");
            } else if ("PLAYER_TURN".equals(dbStatus)) {
                response.setStatus("PLAYER_TURN");
            }
            else if (winner == 1) response.setStatus("PLAYER_WON");
            else if (winner == 2) response.setStatus("COMPUTER_WON");
            else if (gameService.draw(game.getField())) response.setStatus("DRAW");
            else response.setStatus("IN_PROGRESS");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("Game not found: " + gameId, 404));
        }
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<?> joinGame(@PathVariable UUID gameId,
                                      @RequestHeader("X-Player-Id") UUID playerUuid) {
        try {
            GameModel result = gameService.joinGame(gameId, playerUuid);
            GameWebDto response = webMapper.toDto(result);

            response.setPlayer1Id(result.getPlayer1Id());
            response.setPlayer2Id(result.getPlayer2Id());
            response.setCurrentTurnId(result.getCurrentTurnId());
            response.setGameMode(result.getGameMode());

            GameEntity entity = gameRepository.findById(gameId).get();
            response.setStatus(entity.getStatus());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableGames() {
        try {
            Iterable<GameEntity> entities = gameRepository.findAll();

            List<GameWebDto> dtos = StreamSupport.stream(entities.spliterator(), false)
                    .filter(e -> "WAITING".equals(e.getStatus()))
                    .map(entity -> {
                        GameModel model = dataMapper.toModel(entity);
                        GameWebDto dto = webMapper.toDto(model);
                        dto.setPlayer1Id(model.getPlayer1Id());
                        dto.setGameMode(model.getGameMode());
                        dto.setStatus(entity.getStatus());
                        return dto;
                    })
                    .toList();

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to fetch games", 500));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUser(@PathVariable UUID userId) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return ResponseEntity.ok()
                    .body(java.util.Map.of(
                            "id", user.getId(),
                            "login", user.getLogin()
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("User not found", 404));
        }
    }
}