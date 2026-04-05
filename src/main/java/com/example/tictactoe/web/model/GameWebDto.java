package com.example.tictactoe.web.model;

import java.util.UUID;

public class GameWebDto {
    private int[][] board;
    private String status;
    // Кто играет
    private UUID player1Id;      // Игрок за символ "1"
    private UUID player2Id;      // Игрок за символ "2" (может быть null)

    // Состояние игры
    private UUID currentTurnId;  // Чей сейчас ход
    private String gameMode;     // "PVP" или "PVE"
    private UUID winnerId;       // Кто победил (если игра закончена)// Добавляем поле для статуса

    public GameWebDto() {
    }

    public GameWebDto(int[][] board, String status) {
        this.board = board;
        this.status = status;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    // Player 1
    public UUID getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(UUID player1Id) { this.player1Id = player1Id; }

    // Player 2
    public UUID getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(UUID player2Id) { this.player2Id = player2Id; }

     // Current turn
    public UUID getCurrentTurnId() { return currentTurnId; }
    public void setCurrentTurnId(UUID currentTurnId) { this.currentTurnId = currentTurnId; }

    // Game mode
    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }

    // Winner
    public UUID getWinnerId() { return winnerId; }
    public void setWinnerId(UUID winnerId) { this.winnerId = winnerId; }

}