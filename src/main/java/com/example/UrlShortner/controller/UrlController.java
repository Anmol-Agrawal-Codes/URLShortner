package com.example.UrlShortner.controller;

import com.example.UrlShortner.config.CacheStatsLogger;
import com.example.UrlShortner.model.UrlDto;
import com.example.UrlShortner.model.UrlResponseDto;
import com.example.UrlShortner.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.frontend-origin:http://localhost:5500}")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @Autowired
    private CacheStatsLogger cacheStatsLogger;

    @PostMapping("/create")
    public UrlResponseDto createUrl(@RequestBody UrlDto urlDto) {
        return urlService.generateUrl(urlDto);
    }

    @PostMapping(value = "/getUrl", params = "url")
    public UrlResponseDto getUrl(@RequestParam String url) {
        return urlService.getEncodedUrl(url);
    }

    @GetMapping("{shortLink}")
    public ResponseEntity<Object> redirectUrl(@PathVariable String shortLink) {
        String url = urlService.redirectUrl(shortLink);

        cacheStatsLogger.printStats();

        return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                .location(URI.create(url))
                .build();
    }
}
