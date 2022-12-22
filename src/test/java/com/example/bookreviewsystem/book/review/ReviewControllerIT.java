package com.example.bookreviewsystem.book.review;

import com.example.bookreviewsystem.AbstractIntegrationTest;
import com.example.bookreviewsystem.book.management.BookEntity;
import com.example.bookreviewsystem.book.management.BookRepository;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

public class ReviewControllerIT extends AbstractIntegrationTest {

    private static final String ISBN = "9780596004651";

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    /*
    it would be too much to test again the whole process of importing a book because this
    process has already been verified in another integration test. Therefore, here it is
    enough to just populate a book
     */

    @BeforeEach
    public void setup() {
        BookEntity book = new BookEntity();

        book.setPublisher("Duke Inc");
        book.setIsbn(ISBN);
        book.setPages(42L);
        book.setTitle("Joyful testing with Spring Boot");
        book.setDescription("Writing unit and integration test for Spring Boot Applications");
        book.setAuthor("Rieckpil");
        book.setThumbnailUrl("https://rieckpil.de/wp-content/uploads/2020/08/tsbam_introduction_thumbnail-585x329.png.webp");
        book.setGenre("Software Development");

        this.bookRepository.save(book);
    }

    @AfterEach
    public void cleanUp() {
        this.reviewRepository.deleteAll();
    }

    /*
    The integration test has to be more or less black boxed, with no mocking of our interaction with the review verifier.
    Meaning, the actual methods get called.

    For Integration tests, it does not make sense to write several integration tests with all the different
    combinations for BookReview to determine if it is good or not because that should be tested with unit tests.
    Out integration tests should focus more on the high level processes of the application where multiple
    components work/communicate together. We want to see the application as a black box and test different
    interactions our application users will make.
     */

    @Test
    void shouldReturnCreatedReviewWhenBookExistsAndReviewHasGoodQuality() throws JOSEException {
        // Given
        String reviewPayload = """
      {
        "reviewTitle" : "Great book with lots of tips & tricks",
        "reviewContent" : "I can really recommend reading this book. It includes up-to-date library versions and real-world examples",
        "rating" : 4
      }
      """;

        String validJWT = getSignedJWT();

        // When
        // endpoint to add new book to the system
        HttpHeaders responseHeaders = this.webTestClient
                .post()
                .uri("/api/books/{isbn}/reviews", ISBN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reviewPayload)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(ResponseEntity.class)
                .getResponseHeaders();

        // Then
        this.webTestClient
                .get()
                .uri(Objects.requireNonNull(responseHeaders.getLocation()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJWT)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.reviewTitle").isEqualTo("Great book with lots of tips & tricks")
                .jsonPath("$.rating").isEqualTo(4)
                .jsonPath("$.bookIsbn").isEqualTo(ISBN);
    }

    @Test
    void shouldReturnReviewStatisticWhenMultipleReviewsForBookFromDifferentUsersExist() throws JOSEException {
        webTestClient
                .post()
                .uri("/api/books/{isbn}/reviews", ISBN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getSignedJWT("mike", "mike@spring.io"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
      {
        "reviewTitle" : "Great book with lots of tips & tricks",
        "reviewContent" : "I can really recommend reading this book. It includes up-to-date library versions and real-world examples",
        "rating" : 5
      }
      """)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/books/{isbn}/reviews", ISBN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getSignedJWT("duke", "duke@spring.io"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
      {
        "reviewTitle" : "This book is okay",
        "reviewContent" : "I can recommend reading this book for everyone who wants to get started with testing",
        "rating" : 3
      }
      """)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/books/{isbn}/reviews", ISBN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getSignedJWT("mandy", "mandy@spring.io"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
      {
        "reviewTitle" : "This book is great",
        "reviewContent" : "Good content, great example and most of all up-to-date frameworks and libraries",
        "rating" : 4
      }
      """)
                .exchange()
                .expectStatus().isCreated();

        webTestClient
                .get()
                .uri("/api/books/reviews/statistics")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getSignedJWT())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size()").isEqualTo(1)
                .jsonPath("$[0].isbn").isEqualTo(ISBN)
                .jsonPath("$[0].ratings").isEqualTo(3)
                .jsonPath("$[0].avg").isEqualTo(4.00);
    }

}
