package com.example.tictactoe.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;


@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private UUID id;
    @Column(unique = true, nullable = false)
    private String login;
    @Column(nullable = false)
    private String password;

    public UserEntity(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public UserEntity(UUID id, String login, String password){
        this.id = id;
        this.login = login;
        this.password = password;
    }
    public UUID getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
