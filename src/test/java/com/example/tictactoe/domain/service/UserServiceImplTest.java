package com.example.tictactoe.domain.service;

import com.example.tictactoe.datasource.entity.UserEntity;
import com.example.tictactoe.datasource.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void register_shouldReturnTrue_whenNewUser() {
         // Дано: логин свободен
        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$10$hashed");

        // Когда: регистрируем
        boolean result = userService.register("newuser", "pass123");

        // Тогда: успех + пользователь сохранён
        assertTrue(result);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_shouldReturnFalse_whenUserExists() {
        // Дано: пользователь уже есть
        when(userRepository.findByLogin("existing")).thenReturn(
                Optional.of(new UserEntity(UUID.randomUUID(), "existing", "hashed"))
        );

        boolean result = userService.register("existing", "pass123");

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void authorize_shouldReturnUuid_whenCredentialsValid() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, "test", "$2a$10$correctHash");

        when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "$2a$10$correctHash")).thenReturn(true);

        Optional<UUID> result = userService.authorize("test", "pass123");

        assertTrue(result.isPresent());
        assertEquals(userId, result.get());
    }

    @Test
    void authorize_shouldReturnEmpty_whenPasswordWrong() {
        UserEntity user = new UserEntity(UUID.randomUUID(), "test", "$2a$10$correctHash");

        when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "$2a$10$correctHash")).thenReturn(false);

        Optional<UUID> result = userService.authorize("test", "wrongpass");

        assertTrue(result.isEmpty());
    }
}