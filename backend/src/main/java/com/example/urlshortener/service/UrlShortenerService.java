package com.example.urlshortener.service;

import com.example.urlshortener.dto.PagedUrlResponse;
import com.example.urlshortener.dto.UrlShortenRequest;
import com.example.urlshortener.dto.UrlShortenResponse;
import com.example.urlshortener.dto.UrlStatsResponse;

public interface UrlShortenerService {
    UrlShortenResponse shortenUrl(UrlShortenRequest request);
    String resolveOriginalUrl(String shortCode);
    UrlStatsResponse getStats(String shortCode);
    PagedUrlResponse getAllUrls(int page, int size);
}