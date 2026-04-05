package com.example.tictactoe.domain.service;

import com.example.tictactoe.datasource.entity.GameEntity;
import com.example.tictactoe.datasource.entity.StatusGame;
import com.example.tictactoe.datasource.repository.GameRepository;
import com.example.tictactoe.datasource.mapper.GameDataMapper;
import com.example.tictactoe.domain.model.GameField;
import com.example.tictactoe.domain.model.GameModel;

import java.util.UUID;

public class ServiceImpl implements Service {
    private final GameRepository repository;
    private final GameDataMapper dataMapper;

    public ServiceImpl(GameRepository repository, GameDataMapper dataMapper) {
        this.repository = repository;
        this.dataMapper = dataMapper;
    }

    @Override
    public int minmax(GameField field, boolean maximi) {
        if (gameFinished(field)) {
            int winner = getWinner(field);
            if (winner == 2) return 10;
            if (winner == 1) return -10;
            return 0;
        }

        if (maximi) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field.isCellEmpty(i, j)) {
                        field.setValue(i, j, 2);
                        int score = minmax(field, false);
                        field.setValue(i, j, 0);
                        bestScore = Math.max(bestScore, score);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field.isCellEmpty(i, j)) {
                        field.setValue(i, j, 1);
                        int score = minmax(field, true);
                        field.setValue(i, j, 0);
                        bestScore = Math.min(bestScore, score);
                    }
                }
            }
            return bestScore;
        }
    }


    @Override
    public boolean validation(String boardJson, GameField updatedField, int expectedSymbol) {
        // Парсим текущее поле из JSON
        GameField currentField = dataMapper.jsonToField(boardJson);

        int[][] current = currentField.getField();
        int[][] updated = updatedField.getField();

        int changes = 0;
        int changedRow = -1, changedCol = -1;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (current[i][j] != updated[i][j]) {
                    changes++;
                    changedRow = i;
                    changedCol = j;
                }
            }
        }

        if (changes != 1) return false;
        if (current[changedRow][changedCol] != 0) return false;

        if (updated[changedRow][changedCol] != expectedSymbol) return false;

        return true;
    }

    @Override
    public boolean gameFinished(GameField field) {
        return getWinner(field) != 0 || draw(field);
    }

    @Override
    public int[] getBestMove(GameField field) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = new int[]{-1, -1};

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field.isCellEmpty(i, j)) {
                    field.setValue(i, j, 2);
                    int score = minmax(field, false);
                    field.setValue(i, j, 0);

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                }
            }
        }
        return bestMove;
    }

        @Override
        public UUID createNewGame(String gameMode, UUID player1Id) {
            UUID gameId = UUID.randomUUID();
            GameField field = new GameField();
            GameModel newGame = new GameModel(gameId, field);

            StatusGame initialStatus = "PVP".equals(gameMode) ? StatusGame.WAITING : StatusGame.PLAYER_TURN;

            newGame.setPlayer1Id(player1Id);
            newGame.setPlayer2Id("PVE".equals(gameMode) ? null : null); // пока null, присоединится позже
            newGame.setCurrentTurnId(player1Id); // первый ход у создателя
            newGame.setGameMode(gameMode);

            GameEntity entity = dataMapper.toEntity(newGame);
            repository.save(entity);

            return gameId;
        }

    @Override
    public GameModel processMove(GameModel playerGame, UUID playerUuid) {
        if (playerGame.getId() == null) {
            throw new IllegalArgumentException("Game ID cannot be null");
        }

        GameEntity savedEntity = repository.findById(playerGame.getId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        String status = savedEntity.getStatus();
        if ("WIN".equals(status) || "DRAW".equals(status)) {
            throw new IllegalArgumentException("Game is finished");
        }

        if (!savedEntity.getCurrentTurnId().equals(playerUuid)) {
            throw new IllegalArgumentException("Not your turn");
        }

        int expectedSymbol = savedEntity.getPlayer1Id().equals(playerUuid) ? 1 : 2;

        if (!validation(savedEntity.getBoard(), playerGame.getField(), expectedSymbol)) {
            throw new IllegalArgumentException("Invalid move");
        }

        GameField currentField = playerGame.getField();

        int winnerSymbol = getWinner(currentField);
        if (winnerSymbol != 0) {
            savedEntity.setStatus("WIN");
            if (winnerSymbol == 1) {
                savedEntity.setWinnerId(savedEntity.getPlayer1Id());
            } else {
                savedEntity.setWinnerId(savedEntity.getPlayer2Id());
            }
        }
        else if (draw(currentField)) {
            savedEntity.setStatus("DRAW");
        }
        else {
            UUID nextTurn = savedEntity.getCurrentTurnId().equals(savedEntity.getPlayer1Id())
                    ? savedEntity.getPlayer2Id()
                    : savedEntity.getPlayer1Id();
            savedEntity.setCurrentTurnId(nextTurn);


            if ("PVE".equals(savedEntity.getGameMode())) {
                int[] bestMove = getBestMove(currentField);
                if (bestMove[0] != -1) {
                    currentField.setValue(bestMove[0], bestMove[1], 2);
                }

                int compWinner = getWinner(currentField);
                if (compWinner != 0) {
                    savedEntity.setStatus("WIN");
                    savedEntity.setWinnerId(null); // компьютер не имеет UUID
                } else if (draw(currentField)) {
                    savedEntity.setStatus("DRAW");
                }

            } else {

                nextTurn = savedEntity.getCurrentTurnId().equals(savedEntity.getPlayer1Id())
                        ? savedEntity.getPlayer2Id()
                        : savedEntity.getPlayer1Id();
                savedEntity.setCurrentTurnId(nextTurn);
            }}

        savedEntity.setBoard(dataMapper.toEntity(
                new GameModel(savedEntity.getId(), currentField)
        ).getBoard());

        repository.save(savedEntity);
        return dataMapper.toModel(savedEntity);
    }

    @Override
    public boolean draw(GameField field) {
        return !field.hasEmptyCells() && getWinner(field) == 0;
    }

    public boolean win(GameField field, int player) {
        int[][] cell = field.getField();

        for (int i = 0; i < 3; i++) {
            if (cell[i][0] == player && cell[i][1] == player && cell[i][2] == player)
                return true;
        }

        for (int j = 0; j < 3; j++) {
            if (cell[0][j] == player && cell[1][j] == player && cell[2][j] == player)
                return true;
        }

        if (cell[0][0] == player && cell[1][1] == player && cell[2][2] == player)
            return true;
        if (cell[0][2] == player && cell[1][1] == player && cell[2][0] == player)
            return true;

        return false;
    }

    @Override
    public int getWinner(GameField field) {
        if (win(field, 1)) return 1;
        if (win(field, 2)) return 2;
        return 0;
    }
    @Override
    public GameModel joinGame(UUID gameId, UUID player2Id) {
        // 1. Найти игру в БД
        GameEntity entity = repository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        // 2. Проверки
        if (entity.getPlayer2Id() != null) {
            throw new IllegalArgumentException("Game is already full");
        }
        if (!"WAITING".equals(entity.getStatus())) {
            throw new IllegalArgumentException("Game is not waiting for players");
        }
        if (entity.getPlayer1Id().equals(player2Id)) {
            throw new IllegalArgumentException("Cannot join your own game");
        }


        // 3. Обновить Entity
        entity.setPlayer2Id(player2Id);
        entity.setStatus("PLAYER_TURN"); // игра началась
        // currentTurnId уже = player1Id

        // 4. Сохранить
        repository.save(entity);

        // 5. Вернуть модель (для контроллера)
        return dataMapper.toModel(entity);
    }

}