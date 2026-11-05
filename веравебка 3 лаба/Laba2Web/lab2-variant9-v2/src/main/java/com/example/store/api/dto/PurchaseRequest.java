package com.example.store.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на покупку товара")
public record PurchaseRequest(
        @Schema(description = "Сколько штук купить: больше нуля и не больше остатка на складе",
                example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity
) {
}
