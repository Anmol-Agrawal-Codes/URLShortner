package com.example.UrlShortner.error;

import com.example.UrlShortner.model.UrlErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(UrlNotFound.class)
    public ResponseEntity<UrlErrorResponseDto> handleUrlNotFoundException(Exception e) {
        UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto(
                HttpStatus.NOT_FOUND.toString(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(urlErrorResponseDto);
    }

    @ExceptionHandler(InvalidUrl.class)
    public ResponseEntity<UrlErrorResponseDto> handleInvalidUrlException(Exception e) {
        UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto(
                HttpStatus.BAD_REQUEST.toString(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlErrorResponseDto);
    }

    @ExceptionHandler(ExpiredUrl.class)
    public ResponseEntity<UrlErrorResponseDto> handleExpiredUrlException(Exception e) {
        UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto(
                HttpStatus.GONE.toString(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.GONE).body(urlErrorResponseDto);
    }
}
