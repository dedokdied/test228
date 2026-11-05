package com.example.store.domain;

import java.util.Optional;

/**
 * Порт доступа к пользователям.
 */
public interface UserRepository {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    User save(User user);
}
