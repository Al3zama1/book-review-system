package com.example.bookreviewsystem.openlibrary.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenLibraryApiClient {
    private final WebClient openLibraryWebClient;

    public OpenLibraryApiClient(WebClient openLibraryWebClient) {
        this.openLibraryWebClient = openLibraryWebClient;
    }
}
