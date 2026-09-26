package com.example.urlshortener.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlStatsResponse {
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private long clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
}