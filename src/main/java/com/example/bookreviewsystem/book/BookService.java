package com.example.bookreviewsystem.book;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    List<BookEntity> getAllBooks() {
        return null;
    }
}
