package com.elsevier.cardiac_bookmark_service.service;

import com.elsevier.cardiac_bookmark_service.dto.BookmarkResponseDto;

import java.util.List;

public interface BookmarkService {

    List<BookmarkResponseDto> getBookmarks(String userId);

    void deleteBookmark(String userId, String bookmarkId);
}
