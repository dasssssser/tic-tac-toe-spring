package com.example.tictactoe.domain.model;

import java.util.UUID;

public class GameModel {
    private final UUID id;
    private final GameField field;
    private  UUID player1Id;
    private  UUID player2Id;
    private UUID currentTurnId;
    private String gameMode;
    private UUID winnerId;
    private String status;



    public GameModel(UUID id, GameField field) {
        this.id = id;
        this.field = field;}

    public UUID getId() {
        return id;
    }

    public GameField getField() {
        return field;
    }

    public UUID getCurrentTurnId() {
        return currentTurnId;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public String getGameMode() {
        return gameMode;
    }
    public void setPlayer1Id(UUID player1Id) {
        this.player1Id = player1Id;
    }

    public void setPlayer2Id(UUID player2Id) {
        this.player2Id = player2Id;
    }


    public void setCurrentTurnId(UUID currentTurnId) {
        this.currentTurnId = currentTurnId;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
