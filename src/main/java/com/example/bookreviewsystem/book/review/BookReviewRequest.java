package com.example.bookreviewsystem.book.review;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

public record BookReviewRequest(@NotEmpty String reviewTitle,
                                @NotEmpty String reviewContent,
                                @NotNull @PositiveOrZero Integer rating) {
}
