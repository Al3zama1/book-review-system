package com.example.bookreviewsystem.book.review;

import com.example.bookreviewsystem.book.review.RandomReviewParameterResolverExtension.RandomReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RandomReviewParameterResolverExtension.class)
class ReviewVerifierTest {
    private ReviewVerifier reviewVerifier;

    @BeforeEach
    void setup() {
        reviewVerifier = new ReviewVerifier();
    }

    @Test
    void shouldFailWhenReviewContainsSwearWord() {
        // Given
        String review = "This ook is shit";

        // When
        boolean result = reviewVerifier.doesMeetQualityStandards(review);

        // Then
        assertThat(result).withFailMessage("Should detect swear words, but it doesn't").isFalse();
    }

    @Test
    void shouldFailWhenLoremIpsumIsUsed() {
        // Given
        String review = "Lorem ipsum is placeholder text commonly " +
                "used in the graphic, print, and publishing industries for " +
                "previewing layouts and visual mockups";

        // When
        boolean result = reviewVerifier.doesMeetQualityStandards(review);

        // Then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/badReview.csv")
    void shouldFailWhenReviewIsOfBadQuality(String review) {
        // Given --> given review passed through parameter

        // When
        boolean result = reviewVerifier.doesMeetQualityStandards(review);

        // Then
        assertThat(result).isFalse();
    }

    @RepeatedTest(5)
    void shouldFailWhenRandomReviewQualityIsBad(@RandomReview String review) {
        // Given --> review is passed through parameter

        // When
        boolean result = reviewVerifier.doesMeetQualityStandards(review);

        // Then
        assertThat(result).isFalse();

    }

    @Test
    void shouldPassWhenReviewIsGood() {
        // Given
        String review = "I can totally recommend this book to anyone who is interested in learning how to write Java code";

        // When
        boolean result = reviewVerifier.doesMeetQualityStandards(review);

        // Then
        assertThat(result).isTrue();
    }

}