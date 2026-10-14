package com.example.UrlShortner.service;

import com.example.UrlShortner.error.ExpiredUrl;
import com.example.UrlShortner.error.InvalidUrl;
import com.example.UrlShortner.error.UrlNotFound;
import com.example.UrlShortner.model.Url;
import com.example.UrlShortner.model.UrlResponseDto;
import com.example.UrlShortner.repository.UrlRepository;
import com.example.UrlShortner.model.UrlDto;
import com.google.common.hash.Hashing;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class UrlServiceImpl implements UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Override
    public UrlResponseDto generateUrl(UrlDto urlDto) {
        if (!urlDto.originalUrl().isEmpty()) {
            String originalUrl = urlDto.originalUrl();
            String encodedUrl = encodeUrl(originalUrl);
            Url urlToPersist = new Url();
            urlToPersist.setOriginalUrl(originalUrl);
            urlToPersist.setShortLink(encodedUrl);
            urlToPersist.setCreatedAt(LocalDateTime.now());
            urlToPersist.setExpiresAt(getExpiresAt(urlDto.expiresAt(), urlToPersist.getCreatedAt()));
            Url urlToRet = persistUrl(urlToPersist);

            UrlResponseDto urlResponseDto = new UrlResponseDto(
                    originalUrl, encodedUrl, urlToRet.getExpiresAt()
            );
            return urlResponseDto;
        }
        return null;
    }

    private LocalDateTime getExpiresAt(String expirationDate, LocalDateTime creationDate) {
        if(expirationDate == null || expirationDate.isEmpty()) {
            return creationDate.plusMinutes(10);
        }
        return LocalDateTime.parse(expirationDate);
    }

    private String encodeUrl(String url) {
        String encodedUrl = "";
        LocalDateTime time = LocalDateTime.now();
        encodedUrl = Hashing
                . murmur3_32_fixed()
                . hashString(url + time, StandardCharsets.UTF_8)
                .toString();
        return encodedUrl;
    }

    @Override
    public Url persistUrl(Url url) {
        return urlRepository.save(url);
    }

    @Override
    public UrlResponseDto getEncodedUrl(String link) {
        Url url =  urlRepository.findByShortLink(link);
        UrlResponseDto urlResponseDto = new UrlResponseDto(
                url.getOriginalUrl(), url.getShortLink(), url.getExpiresAt()
        );
        return urlResponseDto;
    }

    @Override
    public void deleteUrl(Url url) {
        urlRepository.delete(url);
    }

    @Override
    public String redirectUrl(String url) {

        if(url.isEmpty()) {
            throw new InvalidUrl("Please enter a valid url!");
        }
        Url urlToRet = urlRepository.findByShortLink(url);
        if(urlToRet == null) {
            throw new UrlNotFound("Url Not Found!");
        }
        if(urlToRet.getExpiresAt().isBefore(LocalDateTime.now())) {
            deleteUrl(urlToRet);
            throw new ExpiredUrl("Url is expired!");
        }
        return urlToRet.getOriginalUrl();
    }
}
