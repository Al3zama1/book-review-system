package com.example.bookreviewsystem.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ReviewVerifierTest {

    private ReviewVerifier cut;

    @BeforeEach
    void setup() {
        cut = new ReviewVerifier();
    }

    @Test
    void shouldFailWhenReviewContainsSwearWord() {
        // Given
        String review = "This book is shit";

        // When
        boolean result = cut.doesMeetQualityStandards(review);

        // Then
        assertThat(result).withFailMessage("Test should detect swear words.").isFalse();
    }

    @Test
    void shouldFailWhenReviewContainsLoremIpsum() {
        // Given
        String review = "Lorem ipsum is placeholder text commonly " +
                        "used in the graphic, print, and publishing industries for " +
                        " previewing layouts and visual mockups";

        // When
        boolean result = cut.doesMeetQualityStandards(review);

        // Then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/badReview.csv")
    void shouldFailWhenReviewIsOfBadQuality(String review) {
        // Given -> data provided through parameterized test

        // When
        boolean result = cut.doesMeetQualityStandards(review);

        // Then
        assertThat(result).isFalse();
    }

}