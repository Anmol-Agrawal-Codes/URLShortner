package com.example.UrlShortner.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheStatsLogger {

    private final CacheManager cacheManager;

    public void printStats() {

        Cache cache = cacheManager.getCache("urls");

        if (cache == null) {
            System.out.println("Cache not found");
            return;
        }

        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                (com.github.benmanes.caffeine.cache.Cache<Object, Object>)
                        cache.getNativeCache();

        System.out.println("Cache Stats: " + nativeCache.stats());
        System.out.println("Cache Size: " + nativeCache.estimatedSize());
    }
}
