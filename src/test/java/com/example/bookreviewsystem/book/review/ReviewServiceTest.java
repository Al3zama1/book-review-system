package com.example.bookreviewsystem.book.review;

import com.example.bookreviewsystem.book.management.BookEntity;
import com.example.bookreviewsystem.book.management.BookRepository;
import com.example.bookreviewsystem.exception.BadReviewQualityException;
import com.example.bookreviewsystem.user.UserEntity;
import com.example.bookreviewsystem.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewVerifier mockedReviewverifier;
    @Mock
    private UserService mockedUserService;
    @Mock
    private BookRepository mockedBookRepository;
    @Mock
    private ReviewRepository mockedReviewRepository;

    @InjectMocks
    private ReviewService cut;

    private static final String EMAIL = "duke@spring.io";
    private static final String USERNAME = "duke";
    private static final String ISBN = "42";

    @Test
    void shouldThrowExceptionWhenReviewedBookIsNotExisting() {
        // Given
        given(mockedBookRepository.findByIsbn(ISBN)).willReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> cut.createBookReview(ISBN, null, USERNAME, EMAIL))
                .isInstanceOf(IllegalArgumentException.class);

        // Then
        then(mockedReviewRepository).shouldHaveNoInteractions();
    }

    @Test
    void shouldRejectReviewWhenReviewQualityIsBad() {
        // Given
        BookReviewRequest bookReviewRequest = new BookReviewRequest(
                "Title", "bad content", 1);

        given(mockedBookRepository.findByIsbn(ISBN)).willReturn(Optional.of(new BookEntity()));
        given(mockedReviewverifier.doesMeetQualityStandards(bookReviewRequest.reviewContent())).willReturn(false);

        // When
        assertThatThrownBy(() -> cut.createBookReview(ISBN, bookReviewRequest, USERNAME, EMAIL))
                .isInstanceOf(BadReviewQualityException.class);

        // Then
        then(mockedReviewRepository).shouldHaveNoInteractions();
    }

    @Test
    void shouldStoreReviewWhenReviewQualityIsGoodAndBookIsPresent() {
        // Given
        BookReviewRequest bookReviewRequest = new BookReviewRequest(
                "Title", "good content", 1);

        given(mockedBookRepository.findByIsbn(ISBN)).willReturn(Optional.of(new BookEntity()));
        given(mockedReviewverifier.doesMeetQualityStandards(bookReviewRequest.reviewContent())).willReturn(true);
        given(mockedUserService.getOrCreateUser(USERNAME, EMAIL)).willReturn(new UserEntity());
        given(mockedReviewRepository.save(any(ReviewEntity.class))).willAnswer(invocation -> {
            // the id would be set by DB at runtime, but for testing we have to set it manyally
            // good scenario to use willAnswer from mockito
            ReviewEntity reviewToSave = invocation.getArgument(0);
            reviewToSave.setId(42L);
            return reviewToSave;
        });

        // When
        Long result = cut.createBookReview(ISBN, bookReviewRequest, USERNAME, EMAIL);

        // Then
        assertThat(result).isEqualTo(42);
    }
}