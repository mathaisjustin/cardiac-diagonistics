package com.elsevier.cardiac_bookmark_service.service;

import com.elsevier.cardiac_bookmark_service.dto.BookmarkResponseDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkCacheServiceTest {

    private static final long TTL_SECONDS = 300L;

    @Mock
    private RedisTemplate<String, List<BookmarkResponseDto>> redisTemplate;

    @Mock
    private ValueOperations<String, List<BookmarkResponseDto>> valueOperations;

    private BookmarkCacheService bookmarkCacheService;

    @BeforeEach
    void setUp() {
        bookmarkCacheService = new BookmarkCacheService(redisTemplate, TTL_SECONDS);
    }

    @Test
    void get_delegatesToValueOperationsWithPrefixedKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        List<BookmarkResponseDto> cached = List.of(new BookmarkResponseDto());
        when(valueOperations.get("bookmarks:user-1")).thenReturn(cached);

        List<BookmarkResponseDto> result = bookmarkCacheService.get("user-1");

        assertThat(result).isSameAs(cached);
    }

    @Test
    void put_setsWithPrefixedKeyAndConfiguredTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        List<BookmarkResponseDto> bookmarks = List.of(new BookmarkResponseDto());

        bookmarkCacheService.put("user-1", bookmarks);

        verify(valueOperations).set("bookmarks:user-1", bookmarks, TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void evict_deletesPrefixedKey() {
        bookmarkCacheService.evict("user-1");

        verify(redisTemplate).delete("bookmarks:user-1");
    }
}
