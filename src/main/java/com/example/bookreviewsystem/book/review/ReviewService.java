package com.example.bookreviewsystem.book.review;

import com.example.bookreviewsystem.book.management.BookEntity;
import com.example.bookreviewsystem.book.management.BookRepository;
import com.example.bookreviewsystem.exception.BadReviewQualityException;
import com.example.bookreviewsystem.exception.ReviewNotFoundException;
import com.example.bookreviewsystem.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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
        ArrayNode result = objectMapper.createArrayNode();

        List<ReviewEntity> requestReviews;

        if (orderBy.equals("rating")) {
            requestReviews = reviewRepository.findTop5ByOrderByRatingDescCreatedAtDesc();
        } else {
            requestReviews = reviewRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size));
        }

        requestReviews.stream()
                .map(this::mapReview)
                .forEach(result::add);

        return result;
    }

    private ObjectNode mapReview(ReviewEntity reviewEntity) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("reviewId", reviewEntity.getId());
        objectNode.put("reviewContent", reviewEntity.getContent());
        objectNode.put("reviewTitle", reviewEntity.getTitle());
        objectNode.put("rating", reviewEntity.getRating());
        objectNode.put("bookIsbn", reviewEntity.getBookEntity().getIsbn());
        objectNode.put("bookTitle", reviewEntity.getBookEntity().getTitle());
        objectNode.put("bookThumbnailUrl", reviewEntity.getBookEntity().getThumbnailUrl());
        objectNode.put("submittedBy", reviewEntity.getUserEntity().getName());
        objectNode.put("submittedAt",
                reviewEntity.getCreatedAt().atZone(ZoneId.of("Europe/Berlin")).toInstant().toEpochMilli());
        return objectNode;
    }

    public ArrayNode getReviewStatistics() {
        ArrayNode result = objectMapper.createArrayNode();

        reviewRepository.getReviewStatistics()
                .stream()
                .map(this::mapReviewStatistic)
                .forEach(result::add);

        return result;
    }

    private ObjectNode mapReviewStatistic(ReviewStatistic reviewStatistic) {
        ObjectNode statistic = objectMapper.createObjectNode();
        statistic.put("bookId", reviewStatistic.getId());
        statistic.put("isbn", reviewStatistic.getIsbn());
        statistic.put("avg", reviewStatistic.getAvg());
        statistic.put("ratings", reviewStatistic.getRatings());
        return statistic;
    }

    public Long createBookReview(String isbn, BookReviewRequest bookReviewRequest,
                                 String userName, String email) {
        Optional<BookEntity> bookEntity = bookRepository.findByIsbn(isbn);

        if (bookEntity.isEmpty()) {
            throw new IllegalArgumentException("Book not found");
        }

        if (!reviewVerifier.doesMeetQualityStandards(bookReviewRequest.reviewContent())) {
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
        reviewRepository.deleteByIdAndBookEntityIsbn(reviewId, isbn);
    }

    public ObjectNode getReviewById(String isbn, Long reviewId) {
        return reviewRepository.findByIdAndBookEntityIsbn(reviewId, isbn)
                .map(this::mapReview)
                .orElseThrow(ReviewNotFoundException::new);
    }
}
