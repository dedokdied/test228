package com.example.store.api.dto;

import com.example.store.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Текущий пользователь API")
public record UserResponse(
        @Schema(description = "Идентификатор пользователя", example = "1")
        Long id,

        @Schema(description = "Имя пользователя", example = "admin")
        String username,

        @Schema(description = "Роль пользователя", example = "ADMIN")
        String role
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name());
    }
}
