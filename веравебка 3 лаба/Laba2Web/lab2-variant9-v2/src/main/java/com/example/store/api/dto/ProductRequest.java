package com.example.store.api.dto;

import com.example.store.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные товара для создания или обновления")
public record ProductRequest(
        @Schema(description = "Название товара", example = "Монитор Dell UltraSharp U2723QE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(description = "Бренд производителя", example = "Dell",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String brand,

        @Schema(description = "Цена в рублях, должна быть больше нуля", example = "15999.99",
                minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
        double price,

        @Schema(description = "Количество на складе, не может быть отрицательным", example = "5",
                minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int countInStock
) {

    public Product toProduct() {
        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setCountInStock(countInStock);
        return product;
    }
}
