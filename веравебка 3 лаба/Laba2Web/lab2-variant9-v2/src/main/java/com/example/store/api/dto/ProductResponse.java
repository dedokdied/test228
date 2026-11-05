package com.example.store.api.dto;

import com.example.store.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Товар в ответе сервера")
public record ProductResponse(
        @Schema(description = "Идентификатор товара", example = "1")
        int id,

        @Schema(description = "Название товара", example = "Монитор Dell UltraSharp U2723QE")
        String name,

        @Schema(description = "Бренд производителя", example = "Dell")
        String brand,

        @Schema(description = "Цена в рублях", example = "15999.99")
        double price,

        @Schema(description = "Остаток на складе, шт.", example = "5")
        int countInStock
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getPrice(),
                product.getCountInStock()
        );
    }
}
