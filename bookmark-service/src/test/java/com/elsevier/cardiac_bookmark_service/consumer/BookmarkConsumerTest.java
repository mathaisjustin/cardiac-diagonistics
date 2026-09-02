package com.elsevier.cardiac_bookmark_service.consumer;

import com.elsevier.cardiac_bookmark_service.document.Bookmark;
import com.elsevier.cardiac_bookmark_service.exception.InvalidBookmarkEventException;
import com.elsevier.cardiac_bookmark_service.repository.BookmarkRepository;
import com.elsevier.cardiac_bookmark_service.service.BookmarkCacheService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkConsumerTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private BookmarkCacheService bookmarkCacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void savesNewBookmarkAndEvictsCache() {

        BookmarkConsumer consumer = new BookmarkConsumer(bookmarkRepository, bookmarkCacheService, objectMapper);

        String message = """
                {"userId":"u-1","diagnosisId":"1","payload":{"gender":"Male","age":45,"bp":"130/85","painType":"Typical Angina","treatment":"Medication"}}
                """;

        when(bookmarkRepository.findByUserIdAndDiagnosisId("u-1", "1")).thenReturn(Optional.empty());

        consumer.consumeBookmark(message);

        verify(bookmarkRepository).save(any(Bookmark.class));
        verify(bookmarkCacheService).evict("u-1");
    }

    @Test
    void skipsDuplicateBookmark() {

        BookmarkConsumer consumer = new BookmarkConsumer(bookmarkRepository, bookmarkCacheService, objectMapper);

        String message = """
                {"userId":"u-1","diagnosisId":"1","payload":{"gender":"Male","age":45,"bp":"130/85","painType":"Typical Angina","treatment":"Medication"}}
                """;

        when(bookmarkRepository.findByUserIdAndDiagnosisId("u-1", "1"))
                .thenReturn(Optional.of(Bookmark.newBookmark("u-1", "1")));

        consumer.consumeBookmark(message);

        verify(bookmarkRepository, never()).save(any());
        verify(bookmarkCacheService, never()).evict(any());
    }

    @Test
    void rejectsMalformedMessage() {

        BookmarkConsumer consumer = new BookmarkConsumer(bookmarkRepository, bookmarkCacheService, objectMapper);

        assertThatThrownBy(() -> consumer.consumeBookmark("not json"))
                .isInstanceOf(InvalidBookmarkEventException.class);
    }

    @Test
    void rejectsMessageMissingRequiredFields() {

        BookmarkConsumer consumer = new BookmarkConsumer(bookmarkRepository, bookmarkCacheService, objectMapper);

        assertThatThrownBy(() -> consumer.consumeBookmark("{\"userId\":\"u-1\"}"))
                .isInstanceOf(InvalidBookmarkEventException.class);
    }
}
