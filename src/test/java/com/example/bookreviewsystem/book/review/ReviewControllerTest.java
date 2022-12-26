package com.example.bookreviewsystem.book.review;

import com.example.bookreviewsystem.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import(WebSecurityConfig.class)
class ReviewControllerTest {
    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnTwentyReviewsWithoutAnyOrderWhenNoParametersAreSpecified() throws Exception {
        // Given
        ArrayNode result = objectMapper.createArrayNode();
        ObjectNode statistic = objectMapper.createObjectNode();

        statistic.put("bookId", 1);
        statistic.put("isbn", "42");
        statistic.put("avg", 89.3);
        statistic.put("ratings", 2);

        result.add(statistic);
        given(reviewService.getAllReviews(20, "none")).willReturn(result);

        // When, Then
        mockMvc.perform(get("/api/books/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldNotReturnReviewStatisticsWhenUserIsUnauthenticated() throws Exception {
        // Given

        // When
        mockMvc.perform(get("/api/books/reviews/statistics"))
                .andExpect(status().isUnauthorized());

        // Then
        then(reviewService).shouldHaveNoInteractions();
    }

    @Test
    void shouldReturnReviewStatisticsWhenUserIsAuthenticated() throws Exception {
        // Given

        // When
        mockMvc.perform(get("/api/books/reviews/statistics")
                .with(jwt()))
                .andExpect(status().isOk());

        // Then
        then(reviewService).should().getReviewStatistics();
    }

    @Test
    void shouldCreateNewBookReviewForAuthenticatedUserWithValidPayload() throws Exception {
        // Given
        String requestBody = """
      {
        "reviewTitle": "Great Java Book",
        "reviewContent": "I really like this book!",
        "rating": 4
      }
      """;

        given(reviewService.createBookReview(eq("42"), any(BookReviewRequest.class),
                eq("duke"), endsWith("spring.io"))).willReturn(84L);

        // When
        mockMvc.perform(post("/api/books/{isbn}/reviews", 42)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(jwt().jwt(builder -> builder.claim("email", "duke@spring.io")
                        .claim("preferred_username", "duke"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/books/42/reviews/84")));
    }

    @Test
    void shouldRejectNewBookReviewForAuthenticatedUsersWithInvalidPayload() throws Exception {
        // Given
        String requestBody = """
      {
        "reviewContent": "I really like this book!",
        "rating": -4
      }
      """;

        // When
        mockMvc.perform(post("/api/books/{isbn}/reviews", 42)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(jwt().jwt(builder -> builder.claim("email", "duke@spring.io")
                        .claim("preferred_username", "duke"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldNotAllowDeletingReviewsWhenUserIsAuthenticatedWithoutModeratorRole() throws Exception {
        // Given
        String isbn = "42";
        long reviewId = 3;

        // When
        mockMvc.perform(delete("/api/books/{isbn}/reviews/{reviewId}", isbn, reviewId)
                .with(jwt()))
                .andExpect(status().isForbidden());

        // Then
        then(reviewService).shouldHaveNoInteractions();
    }

    @Test
    void shouldAllowDeletingReviewsWhenUserIsAuthenticatedAndHasModeratorRole() throws Exception {
        // Given
        String isbn = "42";
        long reviewId = 3;

        // When
        mockMvc.perform(delete("/api/books/{isbn}/reviews/{reviewId}", isbn, reviewId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_moderator"))))
                .andExpect(status().isOk());

        // Then
        then(reviewService).should().deleteReview(isbn, reviewId);
    }

}