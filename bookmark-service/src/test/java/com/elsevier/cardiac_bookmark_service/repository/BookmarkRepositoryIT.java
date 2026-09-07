package com.elsevier.cardiac_bookmark_service.repository;

import com.elsevier.cardiac_bookmark_service.document.Bookmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real MongoDB integration test - verifies BookmarkRepository against an actual MongoDB
 * instance via Testcontainers. Requires a Docker daemon to run: `mvn verify` (or `mvn test`)
 * on a machine with Docker, or in CI - this will NOT run in an environment without Docker
 * available.
 *
 * Note: in this Spring Boot 4 version, MongoProperties binds to spring.mongodb.* (not the
 * older spring.data.mongodb.* prefix), matching what the main application.yaml already sets -
 * this override uses the same property for consistency.
 */
@Testcontainers
@DataMongoTest
class BookmarkRepositoryIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Test
    void save_persistsBookmark() {
        Bookmark bookmark = Bookmark.newBookmark("user-1", "diag-1");
        bookmark.setGender("Male");
        bookmark.setAge(45);
        bookmark.setBp("130");
        bookmark.setPainType("Typical Angina");
        bookmark.setTreatment("Medication");

        Bookmark saved = bookmarkRepository.save(bookmark);

        assertThat(bookmarkRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByUserId_returnsOnlyThatUsersBookmarks() {
        bookmarkRepository.save(Bookmark.newBookmark("user-1", "diag-1"));
        bookmarkRepository.save(Bookmark.newBookmark("user-1", "diag-2"));
        bookmarkRepository.save(Bookmark.newBookmark("user-2", "diag-3"));

        List<Bookmark> result = bookmarkRepository.findByUserId("user-1");

        assertThat(result).hasSize(2)
                .extracting(Bookmark::getDiagnosisId)
                .containsExactlyInAnyOrder("diag-1", "diag-2");
    }

    @Test
    void findByUserIdAndDiagnosisId_uniqueLookup() {
        bookmarkRepository.save(Bookmark.newBookmark("user-1", "diag-1"));

        Optional<Bookmark> found = bookmarkRepository.findByUserIdAndDiagnosisId("user-1", "diag-1");

        assertThat(found).isPresent();
    }

    @Test
    void findByIdAndUserId_scopedToOwner() {
        Bookmark saved = bookmarkRepository.save(Bookmark.newBookmark("user-1", "diag-1"));

        assertThat(bookmarkRepository.findByIdAndUserId(saved.getId(), "user-1")).isPresent();
        assertThat(bookmarkRepository.findByIdAndUserId(saved.getId(), "user-2")).isEmpty();
    }
}
