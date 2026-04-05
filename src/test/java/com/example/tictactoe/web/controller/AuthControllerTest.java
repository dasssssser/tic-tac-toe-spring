package com.example.tictactoe.web.controller;

import com.example.tictactoe.domain.service.UserService;
import com.example.tictactoe.web.model.SignUpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    @Test
    void register_shouldReturnOk_whenNewUser() throws Exception {
        SignUpRequest request = new SignUpRequest("testuser", "pass123");

        when(userService.register("testuser", "pass123")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered"));
    }

    @Test
    void register_shouldReturnBadRequest_whenUserExists() throws Exception {
        SignUpRequest request = new SignUpRequest("existing", "pass123");

        when(userService.register("existing", "pass123")).thenReturn(false);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists"));
    }

    @Test
    void login_shouldReturnUuid_whenCredentialsValid() throws Exception {
        String credentials = "testuser:pass123";
        String encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        when(userService.authorize("testuser", "pass123")).thenReturn(Optional.of(testUserId));

        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Basic " + encoded))
                .andExpect(status().isOk())
                .andExpect(content().string(testUserId.toString()));
    }

    @Test
    void login_shouldReturn401_whenInvalidCredentials() throws Exception {
        String credentials = "wrong:wrong";
        String encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        when(userService.authorize("wrong", "wrong")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Basic " + encoded))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid login or password"));
    }

    @Test
    void login_shouldReturn401_whenNoAuthHeader() throws Exception {
        mockMvc.perform(post("/auth/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing Authorization header"));
    }

    @Test
    void login_shouldReturn401_whenInvalidAuthFormat() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Invalid format"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid Authorization format"));
    }
}