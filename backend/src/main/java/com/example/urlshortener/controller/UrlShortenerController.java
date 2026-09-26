package com.example.urlshortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.urlshortener.dto.PagedUrlResponse;
import com.example.urlshortener.dto.UrlShortenRequest;
import com.example.urlshortener.dto.UrlShortenResponse;
import com.example.urlshortener.dto.UrlStatsResponse;
import com.example.urlshortener.service.UrlShortenerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "Endpoints for creating and inspecting shortened URLs")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping
    @Operation(summary = "Create a shortened URL (optionally with a custom alias)")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@Valid @RequestBody UrlShortenRequest request) {
        UrlShortenResponse response = urlShortenerService.shortenUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}/stats")
    @Operation(summary = "Get click analytics for a short code")
    public ResponseEntity<UrlStatsResponse> getStats(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlShortenerService.getStats(shortCode));
    }

    @GetMapping
    @Operation(summary = "List all shortened URLs, paginated, newest first")
    public ResponseEntity<PagedUrlResponse> getAllUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(urlShortenerService.getAllUrls(page, size));
    }
}