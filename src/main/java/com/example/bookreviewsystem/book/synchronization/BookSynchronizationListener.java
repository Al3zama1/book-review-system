package com.example.bookreviewsystem.book.synchronization;

import com.example.bookreviewsystem.book.BookEntity;
import com.example.bookreviewsystem.book.BookRepository;
import com.example.bookreviewsystem.openlibrary.client.OpenLibraryApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;


@Component
public class BookSynchronizationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookSynchronizationListener.class.getName());

    private final BookRepository bookRepository;
    private final OpenLibraryApiClient openLibraryApiClient;

    public BookSynchronizationListener(BookRepository bookRepository, OpenLibraryApiClient openLibraryApiClient) {
        this.bookRepository = bookRepository;
        this.openLibraryApiClient = openLibraryApiClient;
    }

    @SqsListener(value = "$sqs.book-synchronization-queue")
    public void consumeBookUpdates(BookSynchronization bookSynchronization) {
        String isbn = bookSynchronization.getIsbn();
        LOGGER.info("Incoming book update for {}", isbn);

        if (isbn.length() != 13) {
            LOGGER.warn("Incoming isbn for book is not 13 characters long, rejecting it");
            return;
        }

        if (bookRepository.findByIsbn(isbn) != null) {
            LOGGER.debug("Book with isbn {} is already present, rejecting it", isbn);
            return;
        }

        BookEntity bookEntity = openLibraryApiClient.fetchMetadataForBook(isbn);
        bookEntity = bookRepository.save(bookEntity);

        LOGGER.info("Successfully stored new book {}", bookRepository);
    }
}
