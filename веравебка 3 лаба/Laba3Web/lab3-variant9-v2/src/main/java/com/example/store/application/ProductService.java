package com.example.store.application;

import com.example.store.application.exceptions.LimitExceededException;
import com.example.store.application.exceptions.NotEnoughStockException;
import com.example.store.application.exceptions.NotFoundException;
import com.example.store.application.exceptions.ValidationException;
import com.example.store.domain.Product;
import com.example.store.domain.ProductFilter;
import com.example.store.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    public static final int MAX_PRODUCTS = 50;

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Product> getAll(ProductFilter filter) {
        return repository.findAll(filter);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));
    }

    @Transactional
    public Product create(Product product) {
        validate(product);

        if (repository.count() >= MAX_PRODUCTS) {
            throw new LimitExceededException("Достигнут лимит товаров: " + MAX_PRODUCTS);
        }

        return repository.save(product);
    }

    @Transactional
    public Product update(Long id, Product changes) {
        validate(changes);

        Product product = getById(id);
        product.setName(changes.getName());
        product.setBrand(changes.getBrand());
        product.setPrice(changes.getPrice());
        product.setCountInStock(changes.getCountInStock());
        return repository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Товар не найден");
        }
        repository.deleteById(id);
    }

    @Transactional
    public Product buy(Long id, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Количество должно быть больше нуля");
        }

        Product product = getById(id);

        if (product.getCountInStock() < quantity) {
            throw new NotEnoughStockException(
                    "Недостаточно товара на складе: доступно " + product.getCountInStock() + " шт.");
        }

        // Атомарное списание защищает от гонок при одновременных покупках.
        if (!repository.decrementStock(id, quantity)) {
            throw new NotEnoughStockException("Недостаточно товара на складе");
        }

        return getById(id);
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new ValidationException("Поле name обязательно");
        }
        if (product.getBrand() == null || product.getBrand().isBlank()) {
            throw new ValidationException("Поле brand обязательно");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Цена должна быть больше нуля");
        }
        if (product.getCountInStock() < 0) {
            throw new ValidationException("Количество на складе не может быть отрицательным");
        }
    }
}
