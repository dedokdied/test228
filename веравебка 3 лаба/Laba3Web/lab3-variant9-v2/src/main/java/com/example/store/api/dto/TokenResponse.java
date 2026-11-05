package com.example.store.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Выданный JWT-токен")
public record TokenResponse(
        @Schema(description = "Access-токен", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Тип токена", example = "Bearer")
        String tokenType,

        @Schema(description = "Время жизни в секундах", example = "3600")
        long expiresIn,

        @Schema(description = "Имя пользователя", example = "admin")
        String username,

        @Schema(description = "Роль пользователя", example = "ADMIN")
        String role
) {
}
