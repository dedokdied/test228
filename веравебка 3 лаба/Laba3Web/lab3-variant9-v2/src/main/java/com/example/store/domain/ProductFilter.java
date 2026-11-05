package com.example.store.domain;

import java.math.BigDecimal;

/**
 * Фильтр для динамической выборки товаров.
 *
 * <p>Используется для построения запросов через Spring Data JPA Specification
 * (аналог IQueryable/LINQ в .NET): условия комбинируются во время выполнения.</p>
 */
public record ProductFilter(
        String name,
        String brand,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock
) {

    public static ProductFilter empty() {
        return new ProductFilter(null, null, null, null, null);
    }
}
