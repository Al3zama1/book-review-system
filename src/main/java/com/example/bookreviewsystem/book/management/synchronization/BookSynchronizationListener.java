package com.example.bookreviewsystem.book.management.synchronization;

import com.example.bookreviewsystem.book.management.BookEntity;
import com.example.bookreviewsystem.book.management.BookRepository;
import com.example.bookreviewsystem.book.management.client.OpenLibraryApiClient;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookSynchronizationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookSynchronizationListener.class.getName());

    private final BookRepository bookRepository;
    private final OpenLibraryApiClient openLibraryApiClient;

    @Autowired
    public BookSynchronizationListener(BookRepository bookRepository,
                                       OpenLibraryApiClient openLibraryApiClient) {
        this.bookRepository = bookRepository;
        this.openLibraryApiClient = openLibraryApiClient;
    }

    @SqsListener(value = "${sqs.book-synchronization-queue}")
    public void consumeBookUpdates(BookSynchronization bookSynchronization){
        String isbn = bookSynchronization.getIsbn();
        LOGGER.info("Incoming book update for isbn '{}'", isbn);

        if (isbn.length() != 13) {
            LOGGER.warn("Incoming isbn for book is not 13 characters long, rejecting it");
            return;
        }

        if (bookRepository.findByIsbn(isbn).isPresent()) {
            LOGGER.debug("Book with isbn '{}' is already present, rejecting it", isbn);
            return;
        }

        BookEntity newBook = openLibraryApiClient.fetchMetadataForBook(isbn);
        newBook = bookRepository.save(newBook);

        LOGGER.info("Successfully stored new book '{}'", newBook);
    }

}