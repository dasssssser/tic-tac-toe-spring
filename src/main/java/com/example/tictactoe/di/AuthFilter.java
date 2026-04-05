package com.example.tictactoe.di;

import com.example.tictactoe.domain.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final UserService userService;

    public AuthFilter(UserService userService) {
        this.userService = userService;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        System.out.println("AuthFilter - checking path: " + path);
        boolean isPublic = path.equals("/auth/register") || path.equals("/auth/login");
        System.out.println("Is public: " + isPublic);
        return isPublic;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            sendUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        try {
            String base64 = authHeader.substring("Basic ".length());
            String decoded = new String(Base64.getDecoder().decode(base64));
            String[] parts = decoded.split(":", 2);

            if (parts.length != 2) {
                sendUnauthorized(response, "Invalid credentials format");
                return;
            }

            String login = parts[0];
            String password = parts[1];

            Optional<UUID> userId = userService.authorize(login, password);

            if (userId.isPresent()) {
                request.setAttribute("userId", userId.get());
                filterChain.doFilter(request, response);
            } else {
                sendUnauthorized(response, "Invalid login or password");
            }
        } catch (Exception e) {
            sendUnauthorized(response, "Authentication failed");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
    }
}