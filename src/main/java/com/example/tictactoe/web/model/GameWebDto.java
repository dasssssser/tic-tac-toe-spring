package com.example.tictactoe.web.model;

public class GameWebDto {
    private int[][] board;
    private String status;  // Добавляем поле для статуса

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


}