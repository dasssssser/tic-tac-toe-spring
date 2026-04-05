package com.example.tictactoe.di;

import com.example.tictactoe.di.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Конфигурация безопасности приложения
 *
 * @Configuration - указывает Spring, что этот класс содержит Bean-определения
 * @EnableWebSecurity - включает Spring Security в приложении
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,  AuthFilter authFilter) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(authz -> authz
                // Публичные эндпоинты - доступ без авторизации
                .requestMatchers("/auth/**").permitAll()      // логин, регистрация
                .requestMatchers("/public/**").permitAll()    // публичные данные
                .requestMatchers("/swagger-ui/**").permitAll()    // Swagger UI
                .requestMatchers("/v3/api-docs/**").permitAll()   // OpenAPI docs

                .anyRequest().authenticated()  // любой другой URL требует аутентификации
        );

        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Конфигурация CORS - разрешаем запросы из браузера с других доменов
     *
     * CORS необходим, когда фронтенд и бэкенд находятся на разных портах:
     * - фронтенд: http://localhost:3000
     * - бэкенд: http://localhost:8080
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",     // React фронтенд
                "http://localhost:4200",     // Angular фронтенд
                "http://localhost:8081"      // Другой порт
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept", "X-Requested-With"
        ));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}