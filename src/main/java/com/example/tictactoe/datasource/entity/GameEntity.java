package com.example.tictactoe.datasource.entity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "games")
public class GameEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;
        @Column(name ="board")
        private String board;
        @Column(name = "status")
        private String status;
        @Column(name = "player1_id")
        private  UUID player1Id;
        @Column(name = "player2_id")
        private  UUID player2Id;
        @Column(name = "current_turn_id")
        private UUID currentTurnId;
        @Column(name = "game_mode")
        private String gameMode;
        @Column(name = "winner_id")
        private UUID winnerId;

        public GameEntity(UUID id, String board, String status) {
                this.id = id;
                this.board = board;
                this.status = status;
        }

        public GameEntity() {}
        public UUID getId() {
                return id;
        }

        public  String getBoard() {
                return board;  // лучше возвращать копию, но пока оставим
        }

        public String getStatus() {
                return status;
        }

        public void setStatus(String status) {
                this.status = status;
        }
        public void setBoard(String board) {
                this.board = board;
        }
        public UUID getCurrentTurnId() {
                return currentTurnId;
        }

        public UUID getPlayer1Id() {
                return player1Id;
        }

        public UUID getPlayer2Id() {
                return player2Id;
        }

        public UUID getWinnerId() {
                return winnerId;
        }

        public String getGameMode() {
                return gameMode;
        }

        public void setPlayer2Id(UUID player2Id) {
                this.player2Id = player2Id;
        }

        public void setId(UUID id) {
                this.id = id;
        }

        public void setCurrentTurnId(UUID currentTurnId) {
                this.currentTurnId = currentTurnId;
        }

        public void setGameMode(String gameMode) {
                this.gameMode = gameMode;
        }

        public void setPlayer1Id(UUID player1Id) {
                this.player1Id = player1Id;
        }

        public void setWinnerId(UUID winnerId) {
                this.winnerId = winnerId;
        }

}
