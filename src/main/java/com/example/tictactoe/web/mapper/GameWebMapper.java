package com.example.tictactoe.web.mapper;

import com.example.tictactoe.domain.model.GameField;
import com.example.tictactoe.domain.model.GameModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.example.tictactoe.web.model.GameWebDto;

@Mapper(componentModel = "spring")
public interface GameWebMapper {

    @Mapping(source = "field", target = "board", qualifiedByName = "fieldToBoard")
    GameWebDto toDto(GameModel game);

    @Mapping(source = "dto.board", target = "field", qualifiedByName = "boardToField")
    @Mapping(target = "id", source = "gameId")  // Явно указываем маппинг ID
    GameModel toDomain(GameWebDto dto, java.util.UUID gameId);

    @Named("fieldToBoard")
    default int[][] mapFieldToBoard(GameField field) {
        return field != null ? field.getField() : new int[3][3];
    }

    @Named("boardToField")
    default GameField mapBoardToField(int[][] board) {
        return board != null ? new GameField(board) : new GameField();
    }
}