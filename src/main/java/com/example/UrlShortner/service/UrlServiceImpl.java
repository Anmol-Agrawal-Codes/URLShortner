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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UrlServiceImpl implements UrlService {

    private static final Pattern SCHEME_PREFIX = Pattern.compile("^([a-z][a-z0-9+.-]*):", Pattern.CASE_INSENSITIVE);

    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private CachedUrlLookupService cachedUrlLookupService;

    @Override
    public UrlResponseDto generateUrl(UrlDto urlDto) {
        if (urlDto == null || urlDto.originalUrl() == null || urlDto.originalUrl().isBlank()) {
            throw new InvalidUrl("Destination URL is required.");
        }

        String originalUrl = normalizeOriginalUrl(urlDto.originalUrl());
        validateOriginalUrl(originalUrl);

        LocalDateTime createdAt = LocalDateTime.now();
        String encodedUrl = encodeUrl(originalUrl);
        Url urlToPersist = new Url();
        urlToPersist.setOriginalUrl(originalUrl);
        urlToPersist.setShortLink(encodedUrl);
        urlToPersist.setCreatedAt(createdAt);
        urlToPersist.setExpiresAt(getExpiresAt(urlDto.expiresAt(), createdAt));
        Url urlToRet = persistUrl(urlToPersist);

        return new UrlResponseDto(originalUrl, encodedUrl, urlToRet.getExpiresAt());
    }

    private LocalDateTime getExpiresAt(String expirationDate, LocalDateTime creationDate) {
        if (expirationDate == null || expirationDate.isBlank()) {
            return creationDate.plusMinutes(10);
        }

        LocalDateTime expiresAt;
        try {
            expiresAt = LocalDateTime.parse(expirationDate.trim());
        } catch (DateTimeParseException exception) {
            throw new InvalidUrl("Expiration must use the format yyyy-MM-dd'T'HH:mm:ss.");
        }

        if (!expiresAt.isAfter(creationDate)) {
            throw new InvalidUrl("Expiration must be later than the current time.");
        }
        return expiresAt;
    }

    private void validateOriginalUrl(String originalUrl) {
        try {
            URI uri = new URI(originalUrl);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || scheme == null ||
                    (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new InvalidUrl("Enter a valid destination URL beginning with http:// or https://.");
            }
        } catch (URISyntaxException exception) {
            throw new InvalidUrl("Enter a valid destination URL beginning with http:// or https://.");
        }
    }

    private String normalizeOriginalUrl(String originalUrl) {
        String trimmedUrl = originalUrl.trim();
        Matcher schemeMatcher = SCHEME_PREFIX.matcher(trimmedUrl);
        if (schemeMatcher.find()) {
            String scheme = schemeMatcher.group(1);
            String remainder = trimmedUrl.substring(schemeMatcher.end());
            boolean explicitWebUrl = (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && remainder.startsWith("//");
            boolean hostWithPort = remainder.matches("\\d+(?:[/?#].*)?");

            if (!explicitWebUrl && !hostWithPort) {
                throw new InvalidUrl("Enter a domain name or a URL beginning with http:// or https://.");
            }
        }

        return trimmedUrl.matches("(?i)^https?://.*") ? trimmedUrl : "https://" + trimmedUrl;
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
            throw new InvalidUrl("Short link code is required.");
        }

        CachedUrl cachedUrl =
                cachedUrlLookupService.findByShortLink(shortLink);

        if (cachedUrl.expiresAt() != null &&
                cachedUrl.expiresAt().isBefore(LocalDateTime.now())) {

            deleteUrl(shortLink);

            throw new ExpiredUrl("This short link has expired.");
        }

        return cachedUrl.originalUrl();
    }
}
