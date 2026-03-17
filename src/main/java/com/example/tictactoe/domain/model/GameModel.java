package com.example.tictactoe.domain.model;

import java.util.UUID;

public class GameModel {
    private final UUID id;
    private final GameField field;

    public GameModel(UUID id, GameField field){
        this.id = id;
        this.field = field;
    }

    public UUID getId() {
        return id;
    }

    public GameField getField() {
        return field;
    }


}
