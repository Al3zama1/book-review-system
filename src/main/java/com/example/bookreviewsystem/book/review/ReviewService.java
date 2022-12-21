package com.example.bookreviewsystem.book.review;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ReviewService {
    public ArrayNode getAllReviews(Integer size, String orderBy) {
        return null;
    }

    public ArrayNode getReviewStatistics() {
        return null;
    }

    public Long createBookReview(String isbn, BookReviewRequest bookReviewRequest,
                                 String userName, String email) {
        return null;
    }

    public void deleteReview(String isbn, Long reviewId) {

    }
}
