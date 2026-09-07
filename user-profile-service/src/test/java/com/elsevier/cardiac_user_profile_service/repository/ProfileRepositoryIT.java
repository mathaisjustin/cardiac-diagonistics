package com.elsevier.cardiac_user_profile_service.repository;

import com.elsevier.cardiac_user_profile_service.entity.Profile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real MySQL integration test (no H2 substitution) - verifies ProfileRepository and the
 * Profile entity's @PrePersist/@PreUpdate hooks against an actual MySQL instance via
 * Testcontainers. Requires a Docker daemon to run: `mvn verify` (or `mvn test`) on a machine
 * with Docker, or in CI - this will NOT run in an environment without Docker available.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileRepositoryIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("profiles_db")
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
    private ProfileRepository profileRepository;

    private Profile newProfile(String userId) {
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setFirstName("Jane");
        profile.setLastName("Doe");
        profile.setContact("+1-555-0100");
        profile.setDepartment("Cardiology");
        return profile;
    }

    @Test
    void save_generatesProfileIdAndTimestamps() {
        Profile saved = profileRepository.save(newProfile("user-1"));

        assertThat(saved.getProfileId()).isNotBlank();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_found() {
        profileRepository.save(newProfile("user-2"));

        Optional<Profile> found = profileRepository.findById("user-2");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void update_bumpsUpdatedAtTimestamp() throws InterruptedException {
        Profile saved = profileRepository.saveAndFlush(newProfile("user-3"));
        var firstUpdatedAt = saved.getUpdatedAt();

        // Not waiting on an async condition - LocalDateTime.now() has coarser-than-nanosecond
        // resolution on some platforms, so this just guarantees the second saveAndFlush's
        // updatedAt is measurably later than the first.
        Thread.sleep(10); //NOSONAR - deliberate delay for timestamp-ordering assertion, not async polling
        saved.setDepartment("Neurology");
        // saveAndFlush (not save) - plain save() only schedules the update for the next
        // flush, so @PreUpdate wouldn't have run yet and updatedAt would still read back
        // as the original, unchanged value.
        Profile updated = profileRepository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt()).isAfter(firstUpdatedAt);
    }
}
