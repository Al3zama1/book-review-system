package com.example.bookreviewsystem.book.synchronization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookSynchronization {
    private String isbn;

    @JsonCreator
    public BookSynchronization(@JsonProperty("isbn") String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String toString() {
        return "BookUpdate{" +
                "isbn='" + isbn + '\'' +
                '}';
    }


}
