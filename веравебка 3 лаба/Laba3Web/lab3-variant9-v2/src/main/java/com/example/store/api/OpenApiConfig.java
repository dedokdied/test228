package com.example.store.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Настройка Swagger UI: описание API и схема авторизации Bearer JWT.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Electronics Store API",
                version = "3.0.0",
                description = "Лабораторная работа № 3: PostgreSQL, Spring Data JPA, Flyway, JWT, Docker"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
