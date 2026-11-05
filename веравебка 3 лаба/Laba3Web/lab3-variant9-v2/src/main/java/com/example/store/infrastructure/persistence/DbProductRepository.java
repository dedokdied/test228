package com.example.store.infrastructure.persistence;

import com.example.store.domain.Product;
import com.example.store.domain.ProductFilter;
import com.example.store.domain.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Реализация доменного порта поверх Spring Data JPA.
 * Все манипуляции с базой данных выполняются здесь.
 */
@Repository
public class DbProductRepository implements ProductRepository {

    private final ProductJpaRepository jpa;

    public DbProductRepository(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Product> findAll(ProductFilter filter) {
        return jpa.findAll(ProductSpecifications.from(filter), Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Product save(Product product) {
        return jpa.save(product);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(id);
    }

    @Override
    public boolean decrementStock(Long id, int quantity) {
        return jpa.decrementStock(id, quantity) > 0;
    }
}
