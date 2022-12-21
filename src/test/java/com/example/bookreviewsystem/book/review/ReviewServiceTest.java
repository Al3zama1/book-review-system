package com.example.bookreviewsystem.book.review;

import com.example.bookreviewsystem.book.management.BookRepository;
import com.example.bookreviewsystem.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewVerifier reviewverifier;
    @Mock
    private UserService userService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService cut;

    private static final String EMAIL = "duke@spring.io";
    private static final String USERNAME = "duke";
    private static final String ISBN = "42";

    @Test
    void shouldThrowExceptionWhenReviewedBookIsNotExisting() {
        // Given
        given(bookRepository.findByIsbn(ISBN)).willReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> cut.createBookReview(ISBN, null, USERNAME, EMAIL))
                .isInstanceOf(IllegalArgumentException.class);

        // Then
        then(reviewRepository).shouldHaveNoInteractions();
    }
}