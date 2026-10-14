package com.example.UrlShortner.model;

import java.time.LocalDateTime;

public record UrlResponseDto(
        String originalUrl,
        String shortLink,
        LocalDateTime expiresAt
) {
}
