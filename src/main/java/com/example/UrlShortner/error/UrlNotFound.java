package com.example.UrlShortner.error;

import org.springframework.stereotype.Component;

public class UrlNotFound extends RuntimeException {
    public UrlNotFound(String message) {
        super(message);
    }
}
