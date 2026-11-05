package com.example.store.api;

import com.example.store.api.dto.ProductRequest;
import com.example.store.api.dto.ProductResponse;
import com.example.store.api.dto.PurchaseRequest;
import com.example.store.application.ProductService;
import com.example.store.domain.Product;
import com.example.store.domain.ProductFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Validated
@Tag(name = "Products", description = "CRUD магазина электроники. Данные хранятся в PostgreSQL.")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Получить список товаров",
            description = "Возвращает каталог товаров. Доступен анонимно. "
                    + "Поддерживает динамическую фильтрацию: name, brand, minPrice, maxPrice, inStock.")
    @ApiResponse(responseCode = "200", description = "Список товаров успешно получен",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class)),
                    examples = @ExampleObject(name = "Пример ответа",
                            value = "[{\"id\":1,\"name\":\"Монитор Dell UltraSharp U2723QE\",\"brand\":\"Dell\",\"price\":15999.99,\"countInStock\":5}]")))
    public List<ProductResponse> getAll(
            @Parameter(description = "Часть названия товара (без учёта регистра)", example = "монитор")
            @RequestParam(required = false) String name,

            @Parameter(description = "Бренд (без учёта регистра)", example = "Dell")
            @RequestParam(required = false) String brand,

            @Parameter(description = "Минимальная цена", example = "1000")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Максимальная цена", example = "50000")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "true — только в наличии, false — только отсутствующие", example = "true")
            @RequestParam(required = false) Boolean inStock) {

        ProductFilter filter = new ProductFilter(name, brand, minPrice, maxPrice, inStock);
        return service.getAll(filter).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить товар по id",
            description = "Возвращает один товар по его идентификатору. Доступен анонимно.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товар найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Товар с указанным id не найден",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Товар не найден\"}")))
    })
    public ProductResponse getById(
            @Parameter(description = "Идентификатор товара", example = "1", required = true)
            @PathVariable Long id) {
        return ProductResponse.from(service.getById(id));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Добавить товар (только ADMIN)",
            description = "Создаёт новый товар и возвращает его. Лимит магазина — 50 товаров. Требуется роль ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Товар успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации полей или превышен лимит 50 товаров",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Ошибка валидации",
                                            value = "{\"error\":\"Поле name обязательно\"}"),
                                    @ExampleObject(name = "Лимит достигнут",
                                            value = "{\"error\":\"Достигнут лимит товаров: 50\"}")
                            })),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль ADMIN)")
    })
    public ResponseEntity<ProductResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового товара", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Пример запроса",
                                    value = "{\"name\":\"Монитор Dell UltraSharp U2723QE\",\"brand\":\"Dell\",\"price\":15999.99,\"countInStock\":5}")))
            @Valid @RequestBody ProductRequest request) {
        Product created = service.create(request.toProduct());
        return ResponseEntity
                .created(URI.create("/api/products/" + created.getId()))
                .body(ProductResponse.from(created));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Обновить товар по id (только ADMIN)",
            description = "Полностью заменяет поля name, brand, price и countInStock. Требуется роль ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товар успешно обновлён",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации полей",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Цена должна быть больше нуля\"}"))),
            @ApiResponse(responseCode = "404", description = "Товар с указанным id не найден",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Товар не найден\"}"))),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль ADMIN)")
    })
    public ProductResponse update(
            @Parameter(description = "Идентификатор товара", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные товара", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Пример запроса",
                                    value = "{\"name\":\"Монитор Dell UltraSharp U2723QE\",\"brand\":\"Dell\",\"price\":14999.99,\"countInStock\":4}")))
            @Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(service.update(id, request.toProduct()));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Удалить товар по id (только ADMIN)",
            description = "Удаляет товар. При успехе тело ответа пустое. Требуется роль ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Товар успешно удалён",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Товар с указанным id не найден",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Товар не найден\"}"))),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль ADMIN)")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Идентификатор товара", example = "1", required = true)
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/purchase")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Купить товар (USER или ADMIN)",
            description = "Уменьшает остаток на складе и возвращает обновлённый товар. "
                    + "Требуется роль USER или ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Покупка успешно выполнена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверное количество или недостаточно товара на складе",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Неверное количество",
                                            value = "{\"error\":\"Количество должно быть больше нуля\"}"),
                                    @ExampleObject(name = "Недостаточно товара",
                                            value = "{\"error\":\"Недостаточно товара на складе: доступно 3 шт.\"}")
                            })),
            @ApiResponse(responseCode = "404", description = "Товар с указанным id не найден",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Товар не найден\"}"))),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль USER или ADMIN)")
    })
    public ProductResponse buy(
            @Parameter(description = "Идентификатор товара", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сколько штук купить", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Пример запроса",
                                    value = "{\"quantity\":2}")))
            @Valid @RequestBody PurchaseRequest request) {
        return ProductResponse.from(service.buy(id, request.quantity()));
    }
}
