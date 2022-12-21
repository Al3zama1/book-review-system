package com.example.bookreviewsystem.book.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    public List<BookDTO> getAllBooks() {
        List<BookEntity> books = bookRepository.findAll();
        return convertBooks(books);
    }

    private List<BookDTO> convertBooks(List<BookEntity> books) {
        return books.stream().map(i -> new BookDTO(i.getIsbn(), i.getTitle(),
                i.getAuthor(), i.getDescription(), i.getGenre(), i.getPages(),
                i.getPublisher(), i.getThumbnailUrl())).collect(Collectors.toList());
    }


}
