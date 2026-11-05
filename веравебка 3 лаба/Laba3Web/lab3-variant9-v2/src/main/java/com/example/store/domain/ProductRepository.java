package com.example.store.domain;

import java.util.List;
import java.util.Optional;

/**
 * Порт доступа к товарам. Все операции с БД инкапсулированы в реализации
 * (см. {@code DbProductRepository}), сервисный слой не знает о JPA.
 */
public interface ProductRepository {

    List<Product> findAll(ProductFilter filter);

    Optional<Product> findById(Long id);

    Product save(Product product);

    void deleteById(Long id);

    long count();

    boolean existsById(Long id);

    /**
     * Атомарно уменьшает остаток товара на складе.
     *
     * @return {@code true}, если остатка хватило и строка обновлена
     */
    boolean decrementStock(Long id, int quantity);
}
