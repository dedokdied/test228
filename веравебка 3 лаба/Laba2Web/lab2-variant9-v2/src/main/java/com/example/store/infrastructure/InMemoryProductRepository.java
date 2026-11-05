package com.example.store.infrastructure;

import com.example.store.domain.Product;
import com.example.store.domain.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final List<Product> products = new ArrayList<>();
    private int nextId = 1;

    @Override
    public synchronized List<Product> findAll() {
        return new ArrayList<>(products);
    }

    @Override
    public synchronized Optional<Product> findById(int id) {
        return products.stream()
                .filter(product -> product.getId() == id)
                .findFirst();
    }

    @Override
    public synchronized Product save(Product product) {
        product.setId(nextId++);
        products.add(product);
        return product;
    }

    @Override
    public synchronized boolean update(Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == product.getId()) {
                products.set(i, product);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean delete(int id) {
        return products.removeIf(product -> product.getId() == id);
    }

    @Override
    public synchronized int count() {
        return products.size();
    }
}
