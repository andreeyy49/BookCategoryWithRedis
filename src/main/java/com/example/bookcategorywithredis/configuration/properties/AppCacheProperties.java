package com.example.bookcategorywithredis.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.cache")
public class AppCacheProperties {

    private final List<String> cacheNames = new ArrayList<>();

    private final Map<String, CacheProperties> caches = new HashMap<>();

    @Data
    public static class CacheProperties {
        private Duration expire = Duration.ZERO;
    }

    public interface CacheNames {
        String DATABASE_BOOK_BY_TITLE_AND_AUTHOR = "databaseBookByTitleAndAuthor";
        String DATABASE_BOOK_BY_CATEGORY = "databaseBookByCategory";
        String DATABASE_BOOK_BY_ID = "databaseBookById";
    }
}
