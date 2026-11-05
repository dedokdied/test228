package com.example.store.api;

import com.example.store.api.dto.LoginRequest;
import com.example.store.api.dto.TokenResponse;
import com.example.store.api.dto.UserResponse;
import com.example.store.domain.User;
import com.example.store.domain.UserRepository;
import com.example.store.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Аутентификация по JWT и информация о текущем пользователе")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Войти и получить JWT",
            description = "Проверяет логин и пароль, возвращает Bearer-токен. Доступен анонимно.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токен выдан",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Неверный логин или пароль\"}")))
    })
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Пользователь исчез после аутентификации"));

        String token = jwtService.generateToken(user);
        return new TokenResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Информация о текущем пользователе",
            description = "Возвращает логин и роль владельца токена. Требуется аутентификация.")
    @ApiResponse(responseCode = "200", description = "Данные пользователя")
    public UserResponse me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        return UserResponse.from(user);
    }
}
