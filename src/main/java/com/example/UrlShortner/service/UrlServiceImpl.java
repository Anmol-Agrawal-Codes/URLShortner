package com.example.UrlShortner.service;

import com.example.UrlShortner.error.ExpiredUrl;
import com.example.UrlShortner.error.InvalidUrl;
import com.example.UrlShortner.error.UrlNotFound;
import com.example.UrlShortner.model.CachedUrl;
import com.example.UrlShortner.model.Url;
import com.example.UrlShortner.model.UrlResponseDto;
import com.example.UrlShortner.repository.UrlRepository;
import com.example.UrlShortner.model.UrlDto;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class UrlServiceImpl implements UrlService {

    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private CachedUrlLookupService cachedUrlLookupService;

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
    public void deleteUrl(String shortLink) {
        urlRepository.deleteByShortLink(shortLink);
        cachedUrlLookupService.evict(shortLink);
    }

    @Override
    @Transactional(noRollbackFor = ExpiredUrl.class)
    public String redirectUrl(String shortLink) {

        if (shortLink == null || shortLink.isBlank()) {
            throw new InvalidUrl("Please enter a valid URL!");
        }

        CachedUrl cachedUrl =
                cachedUrlLookupService.findByShortLink(shortLink);

        if (cachedUrl.expiresAt() != null &&
                cachedUrl.expiresAt().isBefore(LocalDateTime.now())) {

            deleteUrl(shortLink);

            throw new ExpiredUrl("URL is expired!");
        }

        return cachedUrl.originalUrl();
    }
}
