package com.example.UrlShortner.error;

public class InvalidUrl extends RuntimeException {
    public InvalidUrl(String message) {
        super(message);
    }
}
