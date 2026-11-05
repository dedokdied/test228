package com.example.store.infrastructure.persistence;

import com.example.store.domain.Product;
import com.example.store.domain.ProductFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Построение динамических LINQ-подобных запросов (Criteria API / IQueryable-аналог).
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> from(ProductFilter filter) {
        ProductFilter safe = filter == null ? ProductFilter.empty() : filter;

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(safe.name())) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + safe.name().toLowerCase(Locale.ROOT) + "%"));
            }

            if (hasText(safe.brand())) {
                predicates.add(cb.equal(
                        cb.lower(root.get("brand")),
                        safe.brand().toLowerCase(Locale.ROOT)));
            }

            if (safe.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), safe.minPrice()));
            }

            if (safe.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), safe.maxPrice()));
            }

            if (safe.inStock() != null) {
                predicates.add(safe.inStock()
                        ? cb.greaterThan(root.get("countInStock"), 0)
                        : cb.equal(root.get("countInStock"), 0));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
