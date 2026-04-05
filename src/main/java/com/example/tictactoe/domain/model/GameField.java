package com.example.tictactoe.domain.model;

public class GameField {
    private  int[][] field;
    public GameField(int[][] input){
        this.field = new int[3][3];
        for( int i = 0; i<3; i ++){
            for( int j = 0; j<3; j ++){
                this.field[i][j] = input[i][j];
            }
        }

    }
    public GameField() {
        field = new int[3][3];  // все клетки = 0
    }
        public boolean hasEmptyCells(){

            for(int i =0 ; i< 3; i++){
                for (int j =0; j <3; j++){
                    if(field[i][j] == 0){
                        return true;
                    }
                }
            }
            return false;
        }
        public boolean isCellEmpty(int i, int j){
        return field[i][j] == 0;
        }

        public void setValue( int i, int j, int value){
        field[i][j] = value; // ход 2 1
        }
        public int[][] copy(){
        int[][] copyField = new int[3][3];
        for(int i =0; i<3; i++ ){
            for(int j =0; j<3;j++){
                copyField[i][j] =field[i][j];
            }
        }
        return copyField;
        }

    public int[][] getField() {
        return copy();
    }


    public void setField(int[][] drawBoard) {
        if (drawBoard == null) {
            this.field = new int[3][3];
            return;
        }
        int[][] newField = new int[3][3];
        for (int i = 0; i < 3 && i < drawBoard.length; i++) {
            for (int j = 0; j < 3 && j < drawBoard[i].length; j++) {
                newField[i][j] = drawBoard[i][j];
            }
        }
        this.field = newField;
    }
}
