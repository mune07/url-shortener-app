package com.example.urlshortener.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.urlshortener.dto.PagedUrlResponse;
import com.example.urlshortener.dto.UrlShortenRequest;
import com.example.urlshortener.dto.UrlShortenResponse;
import com.example.urlshortener.dto.UrlStatsResponse;
import com.example.urlshortener.entity.UrlMapping;
import com.example.urlshortener.exception.DuplicateAliasException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.util.Base62Encoder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final UrlMappingRepository repository;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final Set<String> RESERVED_WORDS = Set.of(
            "api", "admin", "swagger-ui", "actuator", "urls", "health"
    );

    @Override
    @Transactional
    public UrlShortenResponse shortenUrl(UrlShortenRequest request) {
        String originalUrl = request.getOriginalUrl().trim();

        if (request.getCustomAlias() == null || request.getCustomAlias().isBlank()) {
            var existing = repository.findByOriginalUrl(originalUrl);
            if (existing.isPresent()) {
                return toShortenResponse(existing.get());
            }
        }

        UrlMapping mapping;

        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            String alias = request.getCustomAlias().trim();

            if (RESERVED_WORDS.contains(alias.toLowerCase())) {
                throw new DuplicateAliasException("'" + alias + "' is a reserved word and cannot be used as an alias");
            }
            if (repository.existsByShortCode(alias)) {
                throw new DuplicateAliasException("Alias '" + alias + "' is already taken");
            }

            mapping = UrlMapping.builder()
                    .originalUrl(originalUrl)
                    .shortCode(alias)
                    .customAlias(true)
                    .clickCount(0)
                    .build();

            mapping = repository.save(mapping);

        } else {
            mapping = UrlMapping.builder()
                    .originalUrl(originalUrl)
                    .shortCode(null)
                    .customAlias(false)
                    .clickCount(0)
                    .build();

            mapping.setShortCode("_tmp_" + System.nanoTime());
            mapping = repository.save(mapping);

            String generatedCode = Base62Encoder.encode(mapping.getId());
            mapping.setShortCode(generatedCode);
            mapping = repository.save(mapping);
        }

        return toShortenResponse(mapping);
    }

    @Override
    @Transactional
    public String resolveOriginalUrl(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("No URL found for code: " + shortCode));

        mapping.setClickCount(mapping.getClickCount() + 1);
        mapping.setLastAccessedAt(LocalDateTime.now());
        repository.save(mapping);

        return mapping.getOriginalUrl();
    }

    @Override
    public UrlStatsResponse getStats(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("No URL found for code: " + shortCode));

        return toStatsResponse(mapping);
    }

    @Override
    public PagedUrlResponse getAllUrls(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UrlMapping> resultPage = repository.findAllByOrderByCreatedAtDesc(pageable);

        List<UrlStatsResponse> content = resultPage.getContent().stream()
                .map(this::toStatsResponse)
                .toList();

        return PagedUrlResponse.builder()
                .content(content)
                .currentPage(resultPage.getNumber())
                .totalPages(resultPage.getTotalPages())
                .totalElements(resultPage.getTotalElements())
                .hasNext(resultPage.hasNext())
                .hasPrevious(resultPage.hasPrevious())
                .build();
    }

    private UrlShortenResponse toShortenResponse(UrlMapping mapping) {
        return UrlShortenResponse.builder()
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .shortUrl(baseUrl + "/" + mapping.getShortCode())
                .createdAt(mapping.getCreatedAt())
                .build();
    }

    private UrlStatsResponse toStatsResponse(UrlMapping mapping) {
        return UrlStatsResponse.builder()
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .shortUrl(baseUrl + "/" + mapping.getShortCode())
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt())
                .lastAccessedAt(mapping.getLastAccessedAt())
                .build();
    }
}