package com.elsevier.cardiac_bookmark_service.service;

import com.elsevier.cardiac_bookmark_service.dto.BookmarkResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis cache for a user's bookmark list. Key layout: {@code bookmarks:<userId>}, one list
 * of {@link BookmarkResponseDto} per user.
 */
@Service
public class BookmarkCacheService {

    private static final String KEY_PREFIX = "bookmarks:";

    private final RedisTemplate<String, List<BookmarkResponseDto>> redisTemplate;
    private final long ttlSeconds;


    public BookmarkCacheService(
            RedisTemplate<String, List<BookmarkResponseDto>> redisTemplate,
            @Value("${bookmark.cache.ttl-seconds}") long ttlSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }


    public List<BookmarkResponseDto> get(String userId) {
        return redisTemplate.opsForValue().get(key(userId));
    }


    public void put(String userId, List<BookmarkResponseDto> bookmarks) {
        redisTemplate.opsForValue().set(key(userId), bookmarks, ttlSeconds, TimeUnit.SECONDS);
    }


    public void evict(String userId) {
        redisTemplate.delete(key(userId));
    }


    private String key(String userId) {
        return KEY_PREFIX + userId;
    }
}
