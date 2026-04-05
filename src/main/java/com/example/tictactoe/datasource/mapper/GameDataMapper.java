package com.example.tictactoe.datasource.mapper;

import com.example.tictactoe.datasource.entity.GameEntity;
import com.example.tictactoe.datasource.entity.StatusGame;
import com.example.tictactoe.domain.model.GameModel;
import com.example.tictactoe.domain.model.GameField;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface GameDataMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(source = "field", target = "board", qualifiedByName = "fieldToJson")
    @Mapping(target = "status", expression = "java(determineStatus(game.getField()).name())")
    GameEntity toEntity(GameModel game);

    // GameEntity → GameModel
    @Mapping(source = "board", target = "field", qualifiedByName = "jsonToField")
    GameModel toModel(GameEntity gameEntity);

    @Named("fieldToJson")
    default String fieldToJson(GameField field) {
        try {
            return OBJECT_MAPPER.writeValueAsString(field.getField());
        } catch (Exception e) {
            return "[[0,0,0],[0,0,0],[0,0,0]]";
        }
    }

    @Named("jsonToField")
    default GameField jsonToField(String board) {
        try {
            int[][] array = OBJECT_MAPPER.readValue(board, int[][].class);
            return new GameField(array);
        } catch (Exception e) {
            return new GameField();
        }
    }

    @Named("determineStatus")
    default StatusGame determineStatus(GameField field) {
        if (field == null) return StatusGame.IN_PROGRESS;

        int[][] cells = field.getField();
        if (cells == null) return StatusGame.IN_PROGRESS;

        if (checkWin(cells, 1)) return StatusGame.WIN;
        if (checkWin(cells, 2)) return StatusGame.WIN;
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