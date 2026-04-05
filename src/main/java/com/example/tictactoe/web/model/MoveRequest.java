package com.example.tictactoe.web.model;

public class MoveRequest {
    private int row;
    private int col;

    public MoveRequest() {}

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
}
