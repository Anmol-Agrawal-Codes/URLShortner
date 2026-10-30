package com.example.UrlShortner.service;

import com.example.UrlShortner.model.UrlDto;
import com.example.UrlShortner.model.Url;
import com.example.UrlShortner.model.UrlResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface UrlService {
    UrlResponseDto generateUrl(UrlDto urlDto);
    Url persistUrl(Url url);
    UrlResponseDto getEncodedUrl(String url);
    void deleteUrl(String shortLink);
    String redirectUrl(String url);
}
