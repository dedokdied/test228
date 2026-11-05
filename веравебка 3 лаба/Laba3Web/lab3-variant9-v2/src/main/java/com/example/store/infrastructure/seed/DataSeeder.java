package com.example.store.infrastructure.seed;

import com.example.store.domain.Role;
import com.example.store.domain.User;
import com.example.store.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Создаёт тестовых пользователей при первом запуске.
 * Товары наполняются SQL-скриптом Flyway (V2__seed_products.sql).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String userPassword;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.admin-password}") String adminPassword,
                      @Value("${app.seed.user-password}") String userPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.userPassword = userPassword;
    }

    @Override
    public void run(String... args) {
        createIfMissing("admin", adminPassword, Role.ADMIN);
        createIfMissing("user", userPassword, Role.USER);
    }

    private void createIfMissing(String username, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        userRepository.save(new User(username, passwordEncoder.encode(rawPassword), role));
        log.info("Создан тестовый пользователь '{}' с ролью {}", username, role);
    }
}
