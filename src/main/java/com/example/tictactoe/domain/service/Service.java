package com.example.tictactoe.domain.service;

import com.example.tictactoe.domain.model.GameField;
import com.example.tictactoe.domain.model.GameModel;

import java.util.UUID;

public interface Service {
     int minmax(GameField field, boolean minmax);
     boolean validation(GameField currentField, GameField updatedField); // добавить метод добавления текущая и прошлая игра
     boolean gameFinished(GameField field);


     public GameModel processMove(GameModel playerGame);
     UUID createNewGame();
     int getWinner(GameField field);
     boolean draw(GameField field);
     int[] getBestMove(GameField field);

}
