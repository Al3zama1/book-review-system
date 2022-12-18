package com.example.bookreviewsystem.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ReviewVerifier reviewVerifier;

    @Autowired
    public ReviewService(ReviewVerifier reviewVerifier) {
        this.reviewVerifier = reviewVerifier;
    }
}
