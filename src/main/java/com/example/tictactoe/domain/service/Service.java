package com.example.tictactoe.domain.service;

import com.example.tictactoe.domain.model.GameField;
import com.example.tictactoe.domain.model.GameModel;

import java.util.UUID;

public interface Service {
     int minmax(GameField field, boolean minmax);
     public boolean validation(String boardJson, GameField updatedField, int expectedSymbol);// добавить метод добавления текущая и прошлая игра
     boolean gameFinished(GameField field);



     public GameModel processMove(GameModel playerGame, UUID playerUuid);
     UUID createNewGame(String gameMode, UUID player1Id);
     int getWinner(GameField field);
     boolean draw(GameField field);
     int[] getBestMove(GameField field);
     public GameModel joinGame(UUID gameId, UUID player2Id);

}
