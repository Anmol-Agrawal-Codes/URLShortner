package com.example.UrlShortner.service;

import com.example.UrlShortner.error.UrlNotFound;
import com.example.UrlShortner.model.CachedUrl;
import com.example.UrlShortner.model.Url;
import com.example.UrlShortner.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachedUrlLookupService {

    private final UrlRepository urlRepository;

    @Cacheable(cacheNames = "urls", key = "#shortLink")
    public CachedUrl findByShortLink(String shortLink) {

        System.out.println("Fetching URL from PostgreSQL");

        Url url = urlRepository.findByShortLink(shortLink);

        if (url == null) {
            throw new UrlNotFound(shortLink);
        }

        return new CachedUrl(
                url.getOriginalUrl(),
                url.getExpiresAt()
        );
    }

    @CacheEvict(cacheNames = "urls", key = "#shortLink")
    public void evict(String shortLink) {
        // Cache entry is evicted by Spring.
    }
}
