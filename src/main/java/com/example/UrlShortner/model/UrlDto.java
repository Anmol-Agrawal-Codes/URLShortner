package com.example.UrlShortner.model;

import jakarta.validation.constraints.NotBlank;

public record UrlDto(
        @NotBlank(message = "Destination URL is required.") String originalUrl,
        String expiresAt
) {
}
