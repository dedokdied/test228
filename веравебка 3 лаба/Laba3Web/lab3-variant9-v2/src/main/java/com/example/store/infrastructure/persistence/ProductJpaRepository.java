package com.example.store.infrastructure.persistence;

import com.example.store.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA-репозиторий товаров.
 *
 * <p>{@link JpaSpecificationExecutor} даёт динамические запросы через Criteria API,
 * а JPQL-запрос ниже — аналог LINQ-выражения для атомарного списания остатка.</p>
 */
public interface ProductJpaRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Product p
               set p.countInStock = p.countInStock - :quantity
             where p.id = :id
               and p.countInStock >= :quantity
            """)
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);
}
