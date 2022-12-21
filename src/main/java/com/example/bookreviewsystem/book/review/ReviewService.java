package com.example.bookreviewsystem.book.review;

import com.example.bookreviewsystem.book.management.BookEntity;
import com.example.bookreviewsystem.book.management.BookRepository;
import com.example.bookreviewsystem.exception.BadReviewQualityException;
import com.example.bookreviewsystem.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {

    private final ReviewVerifier reviewVerifier;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper;

    public ReviewService(ReviewVerifier reviewVerifier, UserService userService,
                         BookRepository bookRepository, ReviewRepository reviewRepository,
                         ObjectMapper objectMapper) {
        this.reviewVerifier = reviewVerifier;
        this.userService = userService;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
        this.objectMapper = objectMapper;
    }

    public ArrayNode getAllReviews(Integer size, String orderBy) {
        return null;
    }

    public ArrayNode getReviewStatistics() {
        return null;
    }

    public Long createBookReview(String isbn, BookReviewRequest bookReviewRequest,
                                 String userName, String email) {
        Optional<BookEntity> bookEntity = bookRepository.findByIsbn(isbn);

        if (bookEntity.isEmpty()) {
            throw new IllegalArgumentException("Book not found");
        }

        if (reviewVerifier.doesMeetQualityStandards(bookReviewRequest.reviewContent())) {
            throw new BadReviewQualityException("Review does not meet quality standards");
        }

        ReviewEntity reviewEntity = ReviewEntity.builder()
                .bookEntity(bookEntity.get())
                .content(bookReviewRequest.reviewContent())
                .title(bookReviewRequest.reviewTitle())
                .rating(bookReviewRequest.rating())
                .userEntity(userService.getOrCreateUser(userName, email))
                .createdAt(LocalDateTime.now()).build();

        reviewEntity = reviewRepository.save(reviewEntity);

        return reviewEntity.getId();
    }

    public void deleteReview(String isbn, Long reviewId) {

    }

    public ObjectNode getReviewById(String isbn, Long reviewId) {
        return null;
    }
}
