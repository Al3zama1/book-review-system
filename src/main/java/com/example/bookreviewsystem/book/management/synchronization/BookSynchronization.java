package com.example.bookreviewsystem.book.management.synchronization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookSynchronization {
    private String isbn;

    @JsonCreator
    public BookSynchronization(@JsonProperty("isbn") String isbn) {
        this.isbn = isbn;
    }
}
