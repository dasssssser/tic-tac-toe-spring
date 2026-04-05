package com.example.tictactoe.domain.service;

import com.example.tictactoe.datasource.entity.UserEntity;
import com.example.tictactoe.datasource.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public boolean register(String login, String password){
        Optional<UserEntity> existing = userRepository.findByLogin(login);
        if(existing.isPresent()){
            return false;
        }
        String encodedPassword = passwordEncoder.encode(password);
        UserEntity newUser = new UserEntity(login, encodedPassword);
        userRepository.save(newUser);
        return true;


    }
    @Override
    public Optional<UUID> authorize(String login, String password){
        Optional<UserEntity> existing = userRepository.findByLogin(login);
        if(existing.isEmpty()){
            return Optional.empty();
        };
        UserEntity user = existing.get();

        if (passwordEncoder.matches(password, user.getPassword())) {

            return Optional.of(user.getId());
        }

        return Optional.empty();
        }



}
