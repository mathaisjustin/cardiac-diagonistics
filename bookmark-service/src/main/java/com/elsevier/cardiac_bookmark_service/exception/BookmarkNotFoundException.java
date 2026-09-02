package com.elsevier.cardiac_bookmark_service.exception;

public class BookmarkNotFoundException extends RuntimeException {

    public BookmarkNotFoundException(String bookmarkId) {
        super("No bookmark found with id: " + bookmarkId);
    }
}
