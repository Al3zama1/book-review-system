package com.example.bookreviewsystem.book.management.client;

import com.example.bookreviewsystem.book.management.BookEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class OpenLibraryApiClient {
    private final WebClient openLibraryWebClient;

    public OpenLibraryApiClient(WebClient openLibraryWebClient) {
        this.openLibraryWebClient = openLibraryWebClient;
    }
    public BookEntity fetchMetadataForBook(String isbn) {
        ObjectNode result = openLibraryWebClient.get().uri("/api/books",
                uriBuilder -> uriBuilder.queryParam("jscmd", "data")
                        .queryParam("format", "json")
                        .queryParam("bibkeys", "ISBN:" + isbn)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
//                .onStatus(HttpStatus::is5xxServerError, error -> Mono.error(new RuntimeException("The system is down")))
                .bodyToMono(ObjectNode.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200)))
                .block();

        JsonNode content = result.get("ISBN:" + isbn);

        return convertToBook(isbn, content);
    }

    private BookEntity convertToBook(String isbn, JsonNode content) {
        return BookEntity.builder()
                .isbn(isbn)
                .thumbnailUrl(content.get("cover").get("small").asText())
                .title(content.get("title").asText())
                .author(content.get("authors").get(0).get("name").asText())
                .publisher(content.get("publishers").get(0).get("name").asText("n.A."))
                .pages(content.get("number_of_pages").asLong(0))
                .description(content.get("notes") == null ? "n.A" : content.get("notes").asText("n.A"))
                .genre(content.get("subjects") == null ? "n.A" : content.get("subjects").get(0).get("name").asText("n.A"))
                .build();
    }
}
