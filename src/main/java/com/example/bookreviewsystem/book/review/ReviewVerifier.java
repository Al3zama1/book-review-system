package com.example.bookreviewsystem.book.review;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ReviewVerifier {

    private boolean doesNotContainSwearWords(String review) {
        return !review.contains("shit");
    }
    public boolean doesMeetQualityStandards(String review) {

        if (review.contains("Lorem ipsum")) return false;

        String[] words = review.split(" ");
        boolean invalid = Arrays.stream(words).filter(word -> word.equalsIgnoreCase("I"))
                .count() >= 5;

        if (invalid) return false;

        invalid = Arrays.stream(words).filter(word -> word.equalsIgnoreCase("good"))
                .count() >= 3;

        if (invalid) return false;

        return doesNotContainSwearWords(review);
    }
}
