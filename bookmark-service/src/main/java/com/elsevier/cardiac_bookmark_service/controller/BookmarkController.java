package com.elsevier.cardiac_bookmark_service.controller;

import com.elsevier.cardiac_bookmark_service.dto.BookmarkResponseDto;
import com.elsevier.cardiac_bookmark_service.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Identity: the API Gateway verifies the JWT and forwards identity downstream
// via the X-User-Id header - this service never sees or decodes a token
// itself, it just trusts that header's presence and value.
@RestController
@RequestMapping("/bookmarks")
@Tag(
        name = "Bookmarks",
        description = "List and remove the authenticated user's bookmarked diagnosis records. "
                + "Both endpoints require identity to be forwarded by the API Gateway via the "
                + "X-User-Id header."
)
public class BookmarkController {

    private final BookmarkService bookmarkService;


    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }


    @Operation(
            summary = "List the authenticated user's bookmarks",
            description = "Returns every bookmark saved by the caller identified by the "
                    + "X-User-Id header forwarded by the Gateway."
    )
    @ApiResponse(responseCode = "200", description = "List of bookmarks",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookmarkResponseDto.class))))
    @GetMapping
    public ResponseEntity<List<BookmarkResponseDto>> getBookmarks(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(bookmarkService.getBookmarks(userId));
    }


    @Operation(
            summary = "Delete a bookmark",
            description = "Removes a bookmark by id, scoped to the caller identified by the "
                    + "X-User-Id header forwarded by the Gateway."
    )
            @ApiResponse(responseCode = "200", description = "Bookmark deleted")
            @ApiResponse(responseCode = "404", description = "No bookmark with the given id for this user",
                    content = @Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Bookmark id", required = true)
            @PathVariable("id") String id
    ) {
        bookmarkService.deleteBookmark(userId, id);
        return ResponseEntity.ok().build();
    }
}
