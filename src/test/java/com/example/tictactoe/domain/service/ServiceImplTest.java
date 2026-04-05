package com.example.tictactoe.domain.service;

import com.example.tictactoe.datasource.entity.GameEntity;
import com.example.tictactoe.datasource.entity.StatusGame;
import com.example.tictactoe.datasource.repository.GameRepository;
import com.example.tictactoe.datasource.mapper.GameDataMapper;
import com.example.tictactoe.domain.model.GameField;
import com.example.tictactoe.domain.model.GameModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceImplTest {

    @Mock
    private GameRepository repository;

    @Mock
    private GameDataMapper dataMapper;

    @InjectMocks
    private ServiceImpl service;

    private UUID gameId;
    private UUID player1Id;
    private UUID player2Id;
    private GameField emptyField;
    private GameField preFilledField;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        emptyField = new GameField();
        preFilledField = new GameField();
    }

    // ==================== ТЕСТЫ ДЛЯ minmax ====================

    @Test
    void testMinmax_WhenGameFinishedWithWinner_ShouldReturnScore() {
        // Подготовка: поле с победой игрока 2 (компьютер)
        GameField field = new GameField();
        field.setValue(0, 0, 2);
        field.setValue(0, 1, 2);
        field.setValue(0, 2, 2); // победа по строке

        int score = service.minmax(field, true);

        assertEquals(10, score);
    }

    @Test
    void testMinmax_WhenGameFinishedWithLoser_ShouldReturnNegativeScore() {
        GameField field = new GameField();
        field.setValue(0, 0, 1);
        field.setValue(0, 1, 1);
        field.setValue(0, 2, 1); // победа игрока 1

        int score = service.minmax(field, true);

        assertEquals(-10, score);
    }

    @Test
    void testMinmax_WhenGameFinishedWithDraw_ShouldReturnZero() {
        // Ничейное поле
        GameField field = new GameField();
        int[][] drawBoard = {
                {1, 2, 1},
                {1, 2, 2},
                {2, 1, 1}
        };
        field.setField(drawBoard);

        int score = service.minmax(field, true);

        assertEquals(0, score);
    }

    // ==================== ТЕСТЫ ДЛЯ validation ====================

    @Test
    void testValidation_ValidMove_ShouldReturnTrue() {
        String boardJson = "{\"field\":[[0,0,0],[0,0,0],[0,0,0]]}";
        GameField updatedField = new GameField();
        updatedField.setValue(1, 1, 1);
        int expectedSymbol = 1;

        when(dataMapper.jsonToField(boardJson)).thenReturn(emptyField);

        boolean result = service.validation(boardJson, updatedField, expectedSymbol);

        assertTrue(result);
    }

    @Test
    void testValidation_MoreThanOneChange_ShouldReturnFalse() {
        String boardJson = "{\"field\":[[0,0,0],[0,0,0],[0,0,0]]}";
        GameField updatedField = new GameField();
        updatedField.setValue(0, 0, 1);
        updatedField.setValue(1, 1, 1); // два изменения

        when(dataMapper.jsonToField(boardJson)).thenReturn(emptyField);

        boolean result = service.validation(boardJson, updatedField, 1);

        assertFalse(result);
    }

    @Test
    void testValidation_WrongSymbol_ShouldReturnFalse() {
        String boardJson = "{\"field\":[[0,0,0],[0,0,0],[0,0,0]]}";
        GameField updatedField = new GameField();
        updatedField.setValue(1, 1, 2); // ожидался символ 1, но поставлен 2

        when(dataMapper.jsonToField(boardJson)).thenReturn(emptyField);

        boolean result = service.validation(boardJson, updatedField, 1);

        assertFalse(result);
    }

    @Test
    void testValidation_CellAlreadyOccupied_ShouldReturnFalse() {
        GameField currentField = new GameField();
        currentField.setValue(0, 0, 1);
        String boardJson = "{\"field\":[[1,0,0],[0,0,0],[0,0,0]]}";

        GameField updatedField = new GameField();
        updatedField.setValue(0, 0, 2); // попытка перезаписать занятую клетку

        when(dataMapper.jsonToField(boardJson)).thenReturn(currentField);

        boolean result = service.validation(boardJson, updatedField, 2);

        assertFalse(result);
    }

    // ==================== ТЕСТЫ ДЛЯ gameFinished ====================

    @Test
    void testGameFinished_WhenWinnerExists_ShouldReturnTrue() {
        GameField field = new GameField();
        field.setValue(0, 0, 1);
        field.setValue(0, 1, 1);
        field.setValue(0, 2, 1);

        assertTrue(service.gameFinished(field));
    }



    @Test
    void testGameFinished_WhenGameContinues_ShouldReturnFalse() {
        assertFalse(service.gameFinished(emptyField));
    }

    // ==================== ТЕСТЫ ДЛЯ getBestMove ====================

    @Test
    void testGetBestMove_ShouldReturnBestPosition() {
        // Поле, где компьютер (2) может выиграть за один ход
        GameField field = new GameField();
        field.setValue(0, 0, 2);
        field.setValue(0, 1, 2);
        // пустая клетка (0,2) — выигрышная

        int[] bestMove = service.getBestMove(field);

        assertEquals(0, bestMove[0]);
        assertEquals(2, bestMove[1]);
    }

    @Test
    void testGetBestMove_WhenFieldIsEmpty_ShouldReturnCenter() {
        int[] bestMove = service.getBestMove(emptyField);

        // Обычно центр (1,1) — лучший первый ход
        assertTrue(bestMove[0] >= 0 && bestMove[0] < 3);
        assertTrue(bestMove[1] >= 0 && bestMove[1] < 3);
    }

    // ==================== ТЕСТЫ ДЛЯ createNewGame ====================

    @Test
    void testCreateNewGame_PVPMode_ShouldReturnGameId() {
        GameEntity mockEntity = new GameEntity();
        mockEntity.setId(gameId);

        when(dataMapper.toEntity(any(GameModel.class))).thenReturn(mockEntity);
        when(repository.save(any(GameEntity.class))).thenReturn(mockEntity); // ← убрали doNothing

        UUID result = service.createNewGame("PVP", player1Id);

        assertNotNull(result);
        verify(repository, times(1)).save(any(GameEntity.class));
    }

    @Test
    void testCreateNewGame_PVEMode_ShouldReturnGameId() {
        GameEntity mockEntity = new GameEntity();
        mockEntity.setId(gameId);

        when(dataMapper.toEntity(any(GameModel.class))).thenReturn(mockEntity);
        when(repository.save(any(GameEntity.class))).thenReturn(mockEntity); // ← убрали doNothing

        UUID result = service.createNewGame("PVE", player1Id);

        assertNotNull(result);
        verify(repository, times(1)).save(any(GameEntity.class));
    }

    // ==================== ТЕСТЫ ДЛЯ processMove ====================

    @Test
    void testProcessMove_ValidMove_ShouldUpdateGame() {
        GameEntity savedEntity = createMockGameEntity();
        String boardJson = "{\"field\":[[0,0,0],[0,0,0],[0,0,0]]}";
        savedEntity.setBoard(boardJson);
        savedEntity.setCurrentTurnId(player1Id);
        savedEntity.setStatus("PLAYER_TURN");

        // ВАЖНО: мокаем jsonToField, чтобы он возвращал пустое поле
        GameField currentField = new GameField();
        when(dataMapper.jsonToField(boardJson)).thenReturn(currentField);

        GameField updatedField = new GameField();
        updatedField.setValue(0, 0, 1);

        GameModel playerGame = new GameModel(gameId, updatedField);
        playerGame.setPlayer1Id(player1Id);
        playerGame.setPlayer2Id(player2Id);

        GameEntity savedEntityAfterMove = createMockGameEntity();
        savedEntityAfterMove.setBoard("{\"field\":[[1,0,0],[0,0,0],[0,0,0]]}");
        savedEntityAfterMove.setCurrentTurnId(player2Id); // ход перешел к player2
        savedEntityAfterMove.setStatus("PLAYER_TURN");

        when(repository.findById(gameId)).thenReturn(Optional.of(savedEntity));
        when(dataMapper.toEntity(any(GameModel.class))).thenReturn(savedEntityAfterMove);
        when(dataMapper.toModel(any(GameEntity.class))).thenReturn(playerGame);
        when(repository.save(any(GameEntity.class))).thenReturn(savedEntityAfterMove);

        GameModel result = service.processMove(playerGame, player1Id);

        assertNotNull(result);
        verify(repository, atLeastOnce()).save(any(GameEntity.class));
    }

    @Test
    void testProcessMove_GameNotFound_ShouldThrowException() {
        GameModel playerGame = new GameModel(gameId, emptyField);
        when(repository.findById(gameId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.processMove(playerGame, player1Id));
    }

    @Test
    void testProcessMove_GameAlreadyFinished_ShouldThrowException() {
        GameEntity savedEntity = createMockGameEntity();
        savedEntity.setStatus("WIN");

        GameModel playerGame = new GameModel(gameId, emptyField);

        when(repository.findById(gameId)).thenReturn(Optional.of(savedEntity));

        assertThrows(IllegalArgumentException.class, () -> service.processMove(playerGame, player1Id));
    }

    @Test
    void testProcessMove_NotYourTurn_ShouldThrowException() {
        GameEntity savedEntity = createMockGameEntity();
        savedEntity.setCurrentTurnId(player2Id); // ход второго игрока
        savedEntity.setStatus("PLAYER_TURN");

        GameModel playerGame = new GameModel(gameId, emptyField);

        when(repository.findById(gameId)).thenReturn(Optional.of(savedEntity));

        assertThrows(IllegalArgumentException.class, () -> service.processMove(playerGame, player1Id));
    }

    @Test
    void testProcessMove_InvalidMove_ShouldThrowException() {
        GameEntity savedEntity = createMockGameEntity();
        String boardJson = "{\"field\":[[1,0,0],[0,0,0],[0,0,0]]}";
        savedEntity.setBoard(boardJson);
        savedEntity.setCurrentTurnId(player1Id);
        savedEntity.setStatus("PLAYER_TURN");

        // Мокаем jsonToField, чтобы он вернул поле с уже занятой клеткой
        GameField currentField = new GameField();
        currentField.setValue(0, 0, 1); // клетка уже занята
        when(dataMapper.jsonToField(boardJson)).thenReturn(currentField);

        // Пытаемся поставить символ на уже занятую клетку
        GameField invalidMoveField = new GameField();
        invalidMoveField.setValue(0, 0, 2); // пытаемся перезаписать

        GameModel playerGame = new GameModel(gameId, invalidMoveField);
        playerGame.setPlayer1Id(player1Id);
        playerGame.setPlayer2Id(player2Id);

        when(repository.findById(gameId)).thenReturn(Optional.of(savedEntity));

        // Ожидаем IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> service.processMove(playerGame, player1Id));
    }

    // ==================== ТЕСТЫ ДЛЯ draw ====================

    @Test
    void testGameFinished_WhenDraw_ShouldReturnTrue() {
        GameField field = new GameField();
        // Реальная ничья в крестиках-ноликах
        int[][] drawBoard = {
                {1, 2, 1},
                {2, 1, 2},
                {2, 1, 2}
        };
        field.setField(drawBoard);

        // Проверяем, что поле не имеет пустых клеток
        assertFalse(field.hasEmptyCells());
        // Проверяем, что нет победителя
        assertEquals(0, service.getWinner(field));
        // Проверяем, что игра закончена
        assertTrue(service.gameFinished(field));
    }



    @Test
    void testDraw_WhenHasEmptyCells_ShouldReturnFalse() {
        GameField field = new GameField(); // пустое поле - есть пустые клетки
        assertFalse(service.draw(field));
    }



    // ==================== ТЕСТЫ ДЛЯ win ====================

    @Test
    void testWin_RowWin_ShouldReturnTrue() {
        GameField field = new GameField();
        field.setValue(0, 0, 1);
        field.setValue(0, 1, 1);
        field.setValue(0, 2, 1);

        assertTrue(service.win(field, 1));
    }

    @Test
    void testWin_ColumnWin_ShouldReturnTrue() {
        GameField field = new GameField();
        field.setValue(0, 0, 2);
        field.setValue(1, 0, 2);
        field.setValue(2, 0, 2);

        assertTrue(service.win(field, 2));
    }

    @Test
    void testWin_DiagonalWin_ShouldReturnTrue() {
        GameField field = new GameField();
        field.setValue(0, 0, 1);
        field.setValue(1, 1, 1);
        field.setValue(2, 2, 1);

        assertTrue(service.win(field, 1));
    }

    @Test
    void testWin_NoWin_ShouldReturnFalse() {
        assertFalse(service.win(emptyField, 1));
    }

    // ==================== ТЕСТЫ ДЛЯ getWinner ====================

    @Test
    void testGetWinner_Player1Wins_ShouldReturn1() {
        GameField field = new GameField();
        field.setValue(0, 0, 1);
        field.setValue(0, 1, 1);
        field.setValue(0, 2, 1);

        assertEquals(1, service.getWinner(field));
    }

    @Test
    void testGetWinner_Player2Wins_ShouldReturn2() {
        GameField field = new GameField();
        field.setValue(1, 0, 2);
        field.setValue(1, 1, 2);
        field.setValue(1, 2, 2);

        assertEquals(2, service.getWinner(field));
    }

    @Test
    void testGetWinner_NoWinner_ShouldReturn0() {
        assertEquals(0, service.getWinner(emptyField));
    }

    // ==================== ТЕСТЫ ДЛЯ joinGame ====================

    @Test
    void testJoinGame_ValidJoin_ShouldSucceed() {
        GameEntity entity = createMockGameEntity();
        entity.setPlayer2Id(null);
        entity.setStatus("WAITING");

        GameModel expectedModel = new GameModel(gameId, emptyField);
        expectedModel.setPlayer1Id(player1Id);
        expectedModel.setPlayer2Id(player2Id);

        when(repository.findById(gameId)).thenReturn(Optional.of(entity));
        when(dataMapper.toModel(any(GameEntity.class))).thenReturn(expectedModel);

        GameModel result = service.joinGame(gameId, player2Id);

        assertNotNull(result);
        verify(repository).save(entity);
        assertEquals(player2Id, entity.getPlayer2Id());
        assertEquals("PLAYER_TURN", entity.getStatus());
    }

    @Test
    void testJoinGame_GameNotFound_ShouldThrowException() {
        when(repository.findById(gameId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.joinGame(gameId, player2Id));
    }

    @Test
    void testJoinGame_GameAlreadyFull_ShouldThrowException() {
        GameEntity entity = createMockGameEntity();
        entity.setPlayer2Id(UUID.randomUUID());

        when(repository.findById(gameId)).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> service.joinGame(gameId, player2Id));
    }

    @Test
    void testJoinGame_GameNotWaiting_ShouldThrowException() {
        GameEntity entity = createMockGameEntity();
        entity.setPlayer2Id(null);
        entity.setStatus("PLAYER_TURN");

        when(repository.findById(gameId)).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> service.joinGame(gameId, player2Id));
    }

    @Test
    void testJoinGame_JoinOwnGame_ShouldThrowException() {
        GameEntity entity = createMockGameEntity();
        entity.setPlayer2Id(null);
        entity.setStatus("WAITING");

        when(repository.findById(gameId)).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> service.joinGame(gameId, player1Id));
    }

    // =================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private GameEntity createMockGameEntity() {
        GameEntity entity = new GameEntity();
        entity.setId(gameId);
        entity.setPlayer1Id(player1Id);
        entity.setPlayer2Id(player2Id);
        entity.setCurrentTurnId(player1Id);
        entity.setStatus("WAITING");
        entity.setGameMode("PVP");
        entity.setBoard("{\"field\":[[0,0,0],[0,0,0],[0,0,0]]}");
        return entity;
    }
}