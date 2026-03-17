package com.example.tictactoe.domain.service;

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
    public boolean validation(GameField currentField, GameField updatedField) {
        int[][] current = currentField.getField();
        int[][] updated = updatedField.getField();

        int changes = 0;
        int changedRow = -1;
        int changedCol = -1;

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
        if (updated[changedRow][changedCol] != 1) return false;

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
    public GameModel processMove(GameModel playerGame) {
        if (playerGame.getId() == null) {
            throw new IllegalArgumentException("Game ID cannot be null");
        }

        GameField currentField = playerGame.getField();

        var savedStorage = repository.findById(playerGame.getId())
                .orElseThrow(() -> new IllegalArgumentException("game not found" + playerGame.getId()));

        GameModel savedGame = dataMapper.toGameModel(savedStorage);
        GameField savedField = savedGame.getField();

        if (!validation(savedField, currentField)) {
            throw new IllegalArgumentException("You need to put a 1 in the empty cell");
        }

        if (gameFinished(currentField)) {
            return playerGame;
        }

        int[] bestMove = getBestMove(currentField);
        if (bestMove[0] != -1 && bestMove[1] != -1) {
            currentField.setValue(bestMove[0], bestMove[1], 2);
        }

        GameModel updatedGame = new GameModel(playerGame.getId(), currentField);
        repository.save(dataMapper.toStorage(updatedGame));

        return updatedGame;
    }

    @Override
    public UUID createNewGame() {
        UUID gameId = UUID.randomUUID();
        GameField field = new GameField();
        GameModel newGame = new GameModel(gameId, field);
        repository.save(dataMapper.toStorage(newGame));
        return gameId;
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
}