package com.example.tictactoe.datasource.mapper;

import com.example.tictactoe.datasource.model.Storage;
import com.example.tictactoe.datasource.model.StatusGame;
import com.example.tictactoe.domain.model.GameModel;
import com.example.tictactoe.domain.model.GameField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public interface GameDataMapper {

    @Mapping(source = "field", target = "board", qualifiedByName = "fieldToBoard")
    @Mapping(target = "status", expression = "java(determineStatus(game.getField()))")
    Storage toStorage(GameModel game);

    @Mapping(source = "board", target = "field", qualifiedByName = "boardToField")
    GameModel toGameModel(Storage storage);

    @Named("fieldToBoard")
    default int[][] mapFieldToBoard(GameField field) {
        if (field == null) return new int[3][3];
        return field.getField();
    }

    @Named("boardToField")
    default GameField mapBoardToField(int[][] board) {
        if (board == null) return new GameField();
        return new GameField(board);
    }

    @Named("determineStatus")
    default StatusGame determineStatus(GameField field) {
        if (field == null) return StatusGame.IN_PROGRESS;

        int[][] cells = field.getField();
        if (cells == null) return StatusGame.IN_PROGRESS;

        if (checkWin(cells, 1)) return StatusGame.PLAYER_WON;
        if (checkWin(cells, 2)) return StatusGame.COMPUTER_WON;
        if (isBoardFull(cells)) return StatusGame.DRAW;
        return StatusGame.IN_PROGRESS;
    }

    private boolean checkWin(int[][] cells, int player) {
        if (cells == null) return false;

        for (int i = 0; i < 3; i++) {
            if (cells[i][0] == player && cells[i][1] == player && cells[i][2] == player) return true;
        }
        for (int j = 0; j < 3; j++) {
            if (cells[0][j] == player && cells[1][j] == player && cells[2][j] == player) return true;
        }
        if (cells[0][0] == player && cells[1][1] == player && cells[2][2] == player) return true;
        if (cells[0][2] == player && cells[1][1] == player && cells[2][0] == player) return true;
        return false;
    }

    private boolean isBoardFull(int[][] cells) {
        if (cells == null) return false;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] == 0) return false;
            }
        }
        return true;
    }
}