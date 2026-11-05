package com.example.store.api.dto;

import com.example.store.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Данные товара для создания или обновления")
public record ProductRequest(
        @Schema(description = "Название товара", example = "Монитор Dell UltraSharp U2723QE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Поле name обязательно")
        String name,

        @Schema(description = "Бренд производителя", example = "Dell",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Поле brand обязательно")
        String brand,

        @Schema(description = "Цена в рублях, должна быть больше нуля", example = "15999.99",
                minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Поле price обязательно")
        @DecimalMin(value = "0.01", message = "Цена должна быть больше нуля")
        BigDecimal price,

        @Schema(description = "Количество на складе, не может быть отрицательным", example = "5",
                minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 0, message = "Количество на складе не может быть отрицательным")
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
