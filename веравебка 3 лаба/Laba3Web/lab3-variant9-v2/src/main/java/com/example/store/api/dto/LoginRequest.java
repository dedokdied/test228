package com.example.store.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные для входа в API")
public record LoginRequest(
        @Schema(description = "Имя пользователя", example = "admin",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Поле username обязательно")
        String username,

        @Schema(description = "Пароль", example = "admin123",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Поле password обязательно")
        String password
) {
}
