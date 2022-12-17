package com.example.bookreviewsystem.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.bookreviewsystem.review.RandomReviewParameterResolverExtension.RandomReview;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(RandomReviewParameterResolverExtension.class)
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

    @RepeatedTest(5)
    void shouldFailWhenRandomReviewQualityIsBad(@RandomReview String review) {
        // Given --> reviews are provided through annotation

        // When
        boolean result = cut.doesMeetQualityStandards(review);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldPassWhenReviewIsGood() {
        // Given
        String review = "I can totally recommend this book to anyone who is interested in learning " +
                        "how to write Java code";

        // When
        boolean result = cut.doesMeetQualityStandards(review);

        // Then
        assertThat(result).isTrue();
    }

}