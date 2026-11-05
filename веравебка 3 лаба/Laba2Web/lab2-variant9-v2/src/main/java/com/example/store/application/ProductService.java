package com.example.store.application;

import com.example.store.application.exceptions.LimitExceededException;
import com.example.store.application.exceptions.NotEnoughStockException;
import com.example.store.application.exceptions.NotFoundException;
import com.example.store.application.exceptions.ValidationException;
import com.example.store.domain.Product;
import com.example.store.domain.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    public static final int MAX_PRODUCTS = 50;

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAll() {
        return repository.findAll();
    }

    public Product getById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));
    }

    public Product create(Product product) {
        validate(product);

        if (repository.count() >= MAX_PRODUCTS) {
            throw new LimitExceededException("Достигнут лимит товаров: " + MAX_PRODUCTS);
        }

        return repository.save(product);
    }

    public Product update(int id, Product product) {
        getById(id);
        validate(product);

        product.setId(id);
        repository.update(product);
        return repository.findById(id).orElseThrow();
    }

    public void delete(int id) {
        if (!repository.delete(id)) {
            throw new NotFoundException("Товар не найден");
        }
    }

    public Product buy(int id, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Количество должно быть больше нуля");
        }

        Product product = getById(id);

        if (product.getCountInStock() < quantity) {
            throw new NotEnoughStockException(
                    "Недостаточно товара на складе: доступно " + product.getCountInStock() + " шт.");
        }

        product.setCountInStock(product.getCountInStock() - quantity);
        repository.update(product);
        return product;
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new ValidationException("Поле name обязательно");
        }
        if (product.getBrand() == null || product.getBrand().isBlank()) {
            throw new ValidationException("Поле brand обязательно");
        }
        if (product.getPrice() <= 0) {
            throw new ValidationException("Цена должна быть больше нуля");
        }
        if (product.getCountInStock() < 0) {
            throw new ValidationException("Количество на складе не может быть отрицательным");
        }
    }
}
