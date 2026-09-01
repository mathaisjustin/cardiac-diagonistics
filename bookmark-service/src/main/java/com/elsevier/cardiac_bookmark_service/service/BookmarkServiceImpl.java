package com.elsevier.cardiac_bookmark_service.service;

import com.elsevier.cardiac_bookmark_service.document.Bookmark;
import com.elsevier.cardiac_bookmark_service.dto.BookmarkResponseDto;
import com.elsevier.cardiac_bookmark_service.exception.BookmarkNotFoundException;
import com.elsevier.cardiac_bookmark_service.repository.BookmarkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkCacheService bookmarkCacheService;


    public BookmarkServiceImpl(
            BookmarkRepository bookmarkRepository,
            BookmarkCacheService bookmarkCacheService
    ) {
        this.bookmarkRepository = bookmarkRepository;
        this.bookmarkCacheService = bookmarkCacheService;
    }


    @Override
    public List<BookmarkResponseDto> getBookmarks(String userId) {

        List<BookmarkResponseDto> cached = bookmarkCacheService.get(userId);

        if (cached != null) {
            return cached;
        }

        List<BookmarkResponseDto> bookmarks = bookmarkRepository.findByUserId(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();

        bookmarkCacheService.put(userId, bookmarks);

        return bookmarks;
    }


    @Override
    public void deleteBookmark(String userId, String bookmarkId) {

        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new BookmarkNotFoundException(bookmarkId));

        bookmarkRepository.delete(bookmark);

        bookmarkCacheService.evict(userId);
    }


    private BookmarkResponseDto toResponseDto(Bookmark bookmark) {

        BookmarkResponseDto dto = new BookmarkResponseDto();

        dto.setId(bookmark.getId());
        dto.setDiagnosisId(bookmark.getDiagnosisId());
        dto.setGender(bookmark.getGender());
        dto.setAge(bookmark.getAge());
        dto.setBp(bookmark.getBp());
        dto.setPainType(bookmark.getPainType());
        dto.setTreatment(bookmark.getTreatment());
        dto.setCreatedAt(bookmark.getCreatedAt());

        return dto;
    }
}
