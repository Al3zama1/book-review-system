package com.example.bookreviewsystem.book.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {


    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookService cut;

    @Test
    void shouldReturnBookData() {
        // Given
        BookEntity bookOne = new BookEntity(1L, "Java 15", "42", "John",
                "Software Engineering", "http://localhost",
                "Book about programming", "Oracle", 100L);

        given(bookRepository.findAll()).willReturn(List.of(bookOne));

        // When
        List<BookDTO> response = cut.getAllBooks();

        // Then
        assertThat(response.get(0).title()).isEqualTo(bookOne.getTitle());
    }

}