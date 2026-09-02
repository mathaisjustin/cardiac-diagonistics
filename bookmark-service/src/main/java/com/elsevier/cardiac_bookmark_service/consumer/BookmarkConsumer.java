package com.elsevier.cardiac_bookmark_service.consumer;

import com.elsevier.cardiac_bookmark_service.document.Bookmark;
import com.elsevier.cardiac_bookmark_service.event.BookmarkEvent;
import com.elsevier.cardiac_bookmark_service.exception.BookmarkPersistenceException;
import com.elsevier.cardiac_bookmark_service.exception.InvalidBookmarkEventException;
import com.elsevier.cardiac_bookmark_service.repository.BookmarkRepository;
import com.elsevier.cardiac_bookmark_service.service.BookmarkCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookmarkConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookmarkConsumer.class);

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkCacheService bookmarkCacheService;
    private final ObjectMapper objectMapper;


    public BookmarkConsumer(
            BookmarkRepository bookmarkRepository,
            BookmarkCacheService bookmarkCacheService,
            ObjectMapper objectMapper
    ) {
        this.bookmarkRepository = bookmarkRepository;
        this.bookmarkCacheService = bookmarkCacheService;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(
            topics = "${bookmark.kafka.topic}",
            groupId = "bookmark-service"
    )
    public void consumeBookmark(String message) {

        BookmarkEvent event;

        try {
            event = objectMapper.readValue(message, BookmarkEvent.class);
        } catch (Exception e) {
            throw new InvalidBookmarkEventException(
                    "Malformed bookmark event, skipping: " + message,
                    e
            );
        }

        if (event.getUserId() == null || event.getDiagnosisId() == null || event.getPayload() == null) {
            throw new InvalidBookmarkEventException(
                    "Bookmark event missing userId, diagnosisId or payload: " + message,
                    null
            );
        }

        try {

            // Idempotent, mirroring POST /bookmarks: an already-bookmarked record is a no-op.
            if (bookmarkRepository.findByUserIdAndDiagnosisId(event.getUserId(), event.getDiagnosisId()).isPresent()) {

                log.info(
                        "Bookmark already exists for user {} / diagnosis {}",
                        event.getUserId(),
                        event.getDiagnosisId()
                );

                return;
            }

            Bookmark bookmark = Bookmark.newBookmark(event.getUserId(), event.getDiagnosisId());

            bookmark.setGender(event.getPayload().getGender());
            bookmark.setAge(event.getPayload().getAge());
            bookmark.setBp(event.getPayload().getBp());
            bookmark.setPainType(event.getPayload().getPainType());
            bookmark.setTreatment(event.getPayload().getTreatment());

            bookmarkRepository.save(bookmark);

            bookmarkCacheService.evict(event.getUserId());

            log.info(
                    "Bookmark saved for user {} / diagnosis {}",
                    event.getUserId(),
                    event.getDiagnosisId()
            );

        } catch (DataAccessException e) {
            throw new BookmarkPersistenceException(
                    "Failed to save bookmark for user: " + event.getUserId(),
                    e
            );
        }
    }
}
