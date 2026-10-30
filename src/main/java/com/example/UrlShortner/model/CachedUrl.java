package com.example.UrlShortner.model;

import java.time.LocalDateTime;

public record CachedUrl(
        String originalUrl,
        LocalDateTime expiresAt
) {}