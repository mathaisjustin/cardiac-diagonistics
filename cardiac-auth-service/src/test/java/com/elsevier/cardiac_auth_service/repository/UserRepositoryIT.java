package com.elsevier.cardiac_auth_service.repository;

import com.elsevier.cardiac_auth_service.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real MySQL integration test (no H2 substitution) - verifies UserRepository's queries and
 * the User entity's @PrePersist id generation against an actual MySQL instance via
 * Testcontainers. Requires a Docker daemon to run: `mvn verify` (or `mvn test`) on a machine
 * with Docker, or in CI - this will NOT run in an environment without Docker available.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("auth_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired
    private UserRepository userRepository;

    private User newUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    void save_generatesIdAndPersists() {
        User saved = userRepository.save(newUser("jane.doe@example.com"));

        assertThat(saved.getId()).isNotBlank();
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByEmail_found() {
        userRepository.save(newUser("john.smith@example.com"));

        Optional<User> found = userRepository.findByEmail("john.smith@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john.smith@example.com");
    }

    @Test
    void findByEmail_notFound_returnsEmpty() {
        assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_trueAfterSave_falseOtherwise() {
        userRepository.save(newUser("exists@example.com"));

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }
}
