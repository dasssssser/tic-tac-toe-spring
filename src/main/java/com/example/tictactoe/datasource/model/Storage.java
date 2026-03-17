package com.example.tictactoe.datasource.model;
import java.util.UUID;

public class Storage {
    private UUID id;
    private int[][] board;
    private StatusGame status;

    public Storage(UUID id, int[][] board, StatusGame status) {
        this.id = id;
        this.board = board;
        this.status = status;
    }


    public UUID getId() {
        return id;
    }

    public int[][] getBoard() {
        return board;  // лучше возвращать копию, но пока оставим
    }

    public StatusGame getStatus() {
        return status;
    }

    public void setStatus(StatusGame status) {
        this.status = status;
    }
}