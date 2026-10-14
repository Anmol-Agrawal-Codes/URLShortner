package com.example.UrlShortner.model;

public record UrlErrorResponseDto(
        String status,
        String errorMsg
) {
}
