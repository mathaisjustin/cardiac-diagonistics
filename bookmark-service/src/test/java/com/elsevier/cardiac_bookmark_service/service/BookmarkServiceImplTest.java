package com.elsevier.cardiac_bookmark_service.service;

import com.elsevier.cardiac_bookmark_service.document.Bookmark;
import com.elsevier.cardiac_bookmark_service.dto.BookmarkResponseDto;
import com.elsevier.cardiac_bookmark_service.exception.BookmarkNotFoundException;
import com.elsevier.cardiac_bookmark_service.repository.BookmarkRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceImplTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private BookmarkCacheService bookmarkCacheService;

    private BookmarkServiceImpl bookmarkService;

    @BeforeEach
    void setUp() {
        bookmarkService = new BookmarkServiceImpl(bookmarkRepository, bookmarkCacheService);
    }

    private Bookmark bookmark(String id, String userId, String diagnosisId) {
        Bookmark bookmark = Bookmark.newBookmark(userId, diagnosisId);
        bookmark.setGender("Male");
        bookmark.setAge(45);
        bookmark.setBp("130");
        bookmark.setPainType("Typical Angina");
        bookmark.setTreatment("Medication");
        return bookmark;
    }

    @Test
    void getBookmarks_cacheHit_returnsCachedWithoutHittingRepository() {
        List<BookmarkResponseDto> cached = List.of(new BookmarkResponseDto());
        when(bookmarkCacheService.get("user-1")).thenReturn(cached);

        List<BookmarkResponseDto> result = bookmarkService.getBookmarks("user-1");

        assertThat(result).isSameAs(cached);
        verify(bookmarkRepository, never()).findByUserId(anyString());
    }

    @Test
    void getBookmarks_cacheMiss_loadsFromRepositoryAndPopulatesCache() {
        when(bookmarkCacheService.get("user-1")).thenReturn(null);
        when(bookmarkRepository.findByUserId("user-1"))
                .thenReturn(List.of(bookmark("b1", "user-1", "1")));

        List<BookmarkResponseDto> result = bookmarkService.getBookmarks("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDiagnosisId()).isEqualTo("1");
        assertThat(result.get(0).getGender()).isEqualTo("Male");
        verify(bookmarkCacheService, times(1)).put(eq("user-1"), any());
    }

    @Test
    void deleteBookmark_found_deletesAndEvictsCache() {
        Bookmark bookmark = bookmark("b1", "user-1", "1");
        when(bookmarkRepository.findByIdAndUserId("b1", "user-1"))
                .thenReturn(Optional.of(bookmark));

        bookmarkService.deleteBookmark("user-1", "b1");

        verify(bookmarkRepository).delete(bookmark);
        verify(bookmarkCacheService).evict("user-1");
    }

    @Test
    void deleteBookmark_notFound_throwsAndNeverEvictsCache() {
        when(bookmarkRepository.findByIdAndUserId("missing", "user-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookmarkService.deleteBookmark("user-1", "missing"))
                .isInstanceOf(BookmarkNotFoundException.class);

        verify(bookmarkRepository, never()).delete(any());
        verify(bookmarkCacheService, never()).evict(anyString());
    }
}
