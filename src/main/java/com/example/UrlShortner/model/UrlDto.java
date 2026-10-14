package com.example.UrlShortner.model;

import jakarta.persistence.Lob;

public record UrlDto(
        @Lob String originalUrl,
        String expiresAt
) {
}
