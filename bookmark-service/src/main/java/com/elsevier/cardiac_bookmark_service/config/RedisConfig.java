package com.elsevier.cardiac_bookmark_service.config;

import com.elsevier.cardiac_bookmark_service.dto.BookmarkResponseDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, List<BookmarkResponseDto>> bookmarkRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {

        ObjectMapper objectMapper = JsonMapper.builder().build();

        RedisTemplate<String, List<BookmarkResponseDto>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJacksonJsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();

        return template;
    }
}
