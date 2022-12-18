package com.example.bookreviewsystem.openlibrary.client;

import com.example.bookreviewsystem.book.BookConverter;
import com.example.bookreviewsystem.book.BookEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class OpenLibraryApiClient {
    private final WebClient openLibraryWebClient;
    private final BookConverter bookConverter;

    public OpenLibraryApiClient(WebClient openLibraryWebClient, BookConverter bookConverter) {
        this.openLibraryWebClient = openLibraryWebClient;
        this.bookConverter = bookConverter;
    }

    public BookEntity fetchMetadataForBook(String isbn) {
        ObjectNode result = openLibraryWebClient.get().uri("/api/books",
                uriBuilder -> uriBuilder.queryParam("jscmd", "data")
                        .queryParam("format", "json")
                        .queryParam("bibkeys", "ISBN:" + isbn)
                        .build())
                .retrieve()
                .bodyToMono(ObjectNode.class)
                // duration is the time to wait before the next try/invocation
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200)))
                .block();

        JsonNode content = result.get("ISBN:" + isbn);

        return bookConverter.convertToBook(isbn, content);
    }

}
