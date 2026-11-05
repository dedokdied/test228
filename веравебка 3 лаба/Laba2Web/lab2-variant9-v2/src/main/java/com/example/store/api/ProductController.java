package com.example.store.api;

import com.example.store.application.ProductService;
import com.example.store.api.dto.ProductRequest;
import com.example.store.api.dto.ProductResponse;
import com.example.store.api.dto.PurchaseRequest;
import com.example.store.domain.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "CRUD магазина электроники. Хранилище в памяти, лимит 50 товаров.")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Получить список всех товаров",
            description = "Возвращает все товары каталога. Если товаров нет — пустой массив.")
    @ApiResponse(responseCode = "200", description = "Список товаров успешно получен",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class)),
                    examples = @ExampleObject(name = "Пример ответа",
                            value = "[{\"id\":1,\"name\":\"Монитор Dell UltraSharp U2723QE\",\"brand\":\"Dell\",\"price\":15999.99,\"countInStock\":5}]")))
    public List<ProductResponse> getAll() {
        return service.getAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить товар по id",
            description = "Возвращает один товар по его идентификатору.")
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
            @PathVariable int id) {
        return ProductResponse.from(service.getById(id));
    }

    @PostMapping
    @Operation(summary = "Добавить товар",
            description = "Создаёт новый товар, присваивает ему id и возвращает созданный товар. Лимит магазина — 50 товаров.")
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
                            }))
    })
    public ResponseEntity<ProductResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового товара", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Пример запроса",
                                    value = "{\"name\":\"Монитор Dell UltraSharp U2723QE\",\"brand\":\"Dell\",\"price\":15999.99,\"countInStock\":5}")))
            @RequestBody ProductRequest request) {
        Product created = service.create(request.toProduct());
        return ResponseEntity
                .created(URI.create("/api/products/" + created.getId()))
                .body(ProductResponse.from(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить товар по id",
            description = "Полностью заменяет поля name, brand, price и countInStock у товара с указанным id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Товар успешно обновлён",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации полей",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Цена должна быть больше нуля\"}"))),
            @ApiResponse(responseCode = "404", description = "Товар с указанным id не найден",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Товар не найден\"}")))
    })
    public ProductResponse update(
            @Parameter(description = "Идентификатор товара", example = "1", required = true)
            @PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные товара", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Пример запроса",
                                    value = "{\"name\":\"Монитор Dell UltraSharp U2723QE\",\"brand\":\"Dell\",\"price\":14999.99,\"countInStock\":4}")))
            @RequestBody ProductRequest request) {
        return ProductResponse.from(service.update(id, request.toProduct()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить товар по id",
            description = "Удаляет товар с указанным id. При успехе тело ответа пустое.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Товар успешно удалён",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Товар с указанным id не найден",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Товар не найден\"}")))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Идентификатор товара", example = "1", required = true)
            @PathVariable int id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/purchase")
    @Operation(summary = "Купить товар",
            description = "Уменьшает остаток товара на складе на указанное количество и возвращает обновлённый товар. Нельзя купить больше, чем есть на складе.")
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
                            examples = @ExampleObject(value = "{\"error\":\"Товар не найден\"}")))
    })
    public ProductResponse buy(
            @Parameter(description = "Идентификатор товара", example = "1", required = true)
            @PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сколько штук купить", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Пример запроса",
                                    value = "{\"quantity\":2}")))
            @RequestBody PurchaseRequest request) {
        return ProductResponse.from(service.buy(id, request.quantity()));
    }
}
