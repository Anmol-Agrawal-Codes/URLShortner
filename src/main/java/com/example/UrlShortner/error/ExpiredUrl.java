package com.example.UrlShortner.error;

public class ExpiredUrl extends RuntimeException {
    public ExpiredUrl(String message) {
        super(message);
    }
}
