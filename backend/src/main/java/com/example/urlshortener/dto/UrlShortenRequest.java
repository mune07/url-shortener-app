package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrlShortenRequest {

    @NotBlank(message = "URL must not be blank")
    @Pattern(
            regexp = "^(https?://)[\\w.-]+(\\.[a-zA-Z]{2,})+([/\\w\\-._~:?#\\[\\]@!$&'()*+,;=%]*)?$",
            message = "Must be a valid http/https URL"
    )
    private String originalUrl;

    @Pattern(
            regexp = "^[a-zA-Z0-9_-]{3,20}$",
            message = "Alias must be 3-20 characters, letters/numbers/underscore/hyphen only"
    )
    private String customAlias;
}