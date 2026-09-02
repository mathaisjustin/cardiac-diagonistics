package com.elsevier.cardiac_bookmark_service.controller;

import com.elsevier.cardiac_bookmark_service.event.BookmarkEvent;
import com.elsevier.cardiac_bookmark_service.producer.MockBookmarkProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only endpoint for publishing a sample {@code bookmark.created} Kafka event, so the
 * consumer can be exercised before the real producer (Diagnosis Service / frontend flow)
 * exists. Not part of the documented public API.
 */
@RestController
@RequestMapping("/internal/mock")
public class MockBookmarkController {

    private final MockBookmarkProducer mockBookmarkProducer;


    public MockBookmarkController(MockBookmarkProducer mockBookmarkProducer) {
        this.mockBookmarkProducer = mockBookmarkProducer;
    }


    @PostMapping("/bookmarks")
    public ResponseEntity<Void> publishMockBookmark(@RequestBody BookmarkEvent event) {
        mockBookmarkProducer.publish(event);
        return ResponseEntity.accepted().build();
    }
}
