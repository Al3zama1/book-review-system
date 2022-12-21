package com.example.bookreviewsystem.book.management.synchronization;

import com.example.bookreviewsystem.book.management.BookEntity;
import com.example.bookreviewsystem.book.management.BookRepository;
import com.example.bookreviewsystem.book.management.client.OpenLibraryApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BookSynchronizationListenerTest {

    private final static String VALID_ISBN = "1234567891234";

    @Mock
    private BookRepository bookRepository;

    @Mock
    private OpenLibraryApiClient openLibraryApiClient;

    @InjectMocks
    private BookSynchronizationListener cut;

    @Captor
    private ArgumentCaptor<BookEntity> bookArgumentCaptor;

    @Test
    void shouldRejectBookWhenIsbnIsMalformed() {
        // Given
        BookSynchronization bookSynchronization = new BookSynchronization("42");

        // When
        cut.consumeBookUpdates(bookSynchronization);

        // Then
        then(openLibraryApiClient).shouldHaveNoInteractions();
        then(bookRepository).shouldHaveNoInteractions();

    }

    @Test
    void shouldNotOverrideWhenBookAlreadyExists() {
        // Given
        BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);
        given(bookRepository.findByIsbn(bookSynchronization.getIsbn())).willReturn(Optional.of(new BookEntity()));

        // When
        cut.consumeBookUpdates(bookSynchronization);

        // Then
        then(openLibraryApiClient).shouldHaveNoInteractions();
        then(bookRepository).should(never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProcessingFails() {
        // Given
        BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);
        given(bookRepository.findByIsbn(bookSynchronization.getIsbn())).willReturn(Optional.empty());

        given(openLibraryApiClient.fetchMetadataForBook(VALID_ISBN))
                .willThrow(new RuntimeException("Network timeout"));

        // When
        assertThatThrownBy(() -> cut.consumeBookUpdates(bookSynchronization))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Network timeout");


        // Then
        then(bookRepository).should(never()).save(any());
    }

    @Test
    void shouldStoreBookWhenNewAndCorrectIsbn() {
        // Given
        BookSynchronization bookSynchronization = new BookSynchronization(VALID_ISBN);
        BookEntity book = new BookEntity();
        book.setTitle("Java book");
        book.setIsbn(VALID_ISBN);

        given(bookRepository.findByIsbn(bookSynchronization.getIsbn())).willReturn(Optional.empty());
        given(openLibraryApiClient.fetchMetadataForBook(VALID_ISBN)).willReturn(book);

        // When
        cut.consumeBookUpdates(bookSynchronization);

        // Then
        then(bookRepository).should().save(bookArgumentCaptor.capture());
        BookEntity savedBook = bookArgumentCaptor.getValue();
        assertThat(savedBook)
                .withFailMessage("ReviewVerifier did not pass a good review")
                .isEqualTo(book);
    }

}