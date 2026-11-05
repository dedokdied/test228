package com.example.store.infrastructure.persistence;

import com.example.store.domain.User;
import com.example.store.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DbUserRepository implements UserRepository {

    private final UserJpaRepository jpa;

    public DbUserRepository(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public User save(User user) {
        return jpa.save(user);
    }
}
