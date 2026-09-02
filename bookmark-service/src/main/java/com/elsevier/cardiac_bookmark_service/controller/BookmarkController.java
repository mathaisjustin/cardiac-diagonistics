package com.elsevier.cardiac_bookmark_service.controller;

import com.elsevier.cardiac_bookmark_service.dto.BookmarkResponseDto;
import com.elsevier.cardiac_bookmark_service.service.BookmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;


    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }


    @GetMapping
    public ResponseEntity<List<BookmarkResponseDto>> getBookmarks(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(bookmarkService.getBookmarks(userId));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") String id
    ) {
        bookmarkService.deleteBookmark(userId, id);
        return ResponseEntity.ok().build();
    }
}
