package com.example.UrlShortner.service;

import com.example.UrlShortner.model.UrlDto;
import com.example.UrlShortner.model.Url;
import com.example.UrlShortner.model.UrlResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface UrlService {
    UrlResponseDto generateUrl(UrlDto urlDto);
    Url persistUrl(Url url);
    UrlResponseDto getEncodedUrl(String url);
    void deleteUrl(Url url);
    String redirectUrl(String url);
}
