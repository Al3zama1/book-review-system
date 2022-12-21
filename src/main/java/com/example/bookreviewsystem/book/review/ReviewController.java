package com.example.bookreviewsystem.book.review;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews")
    public ArrayNode getAllReviews(@RequestParam(name = "size", defaultValue = "20") Integer size,
                                   @RequestParam(name = "orderBy", defaultValue = "none") String orderBy) {
        return reviewService.getAllReviews(size, orderBy);
    }

    @GetMapping("/reviews/statistics")
    public ArrayNode getReviewStatistics() {
        return reviewService.getReviewStatistics();
    }

    @PostMapping("/{isbn}/reviews")
    public ResponseEntity<Void> createBookReview(@PathVariable String isbn, JwtAuthenticationToken jwt,
                                                 @RequestBody @Valid BookReviewRequest bookReviewRequest,
                                                 UriComponentsBuilder uriComponentsBuilder) {
        Long reviewId = reviewService.createBookReview(isbn, bookReviewRequest,
                jwt.getTokenAttributes().get("preferred_username").toString(),
                jwt.getTokenAttributes().get("email").toString());

        UriComponents uriComponents = uriComponentsBuilder.path("/api/books/{isbn}/reviews/{reviewId}")
                .buildAndExpand(isbn, reviewId);

        return ResponseEntity.created(uriComponents.toUri()).build();
    }

    @DeleteMapping("/{isbn}/reviews/{reviewId}")
    public void deleteBookReview(@PathVariable String isbn, @PathVariable Long reviewId) {
        reviewService.deleteReview(isbn, reviewId);
    }

}
