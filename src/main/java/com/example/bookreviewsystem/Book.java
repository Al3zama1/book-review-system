package com.example.bookreviewsystem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Book {
    @Id
    @JsonIgnore
    @GeneratedValue(
            strategy = GenerationType.IDENTITY,
            generator = "book_sequence"
    )
    private Long id;
    @Column(nullable = false)
    private String title;
    @NaturalId
    private String isbn;
    private String genre;
    private String thumbnailUrl;
    private String description;
    private String publisher;
    private Integer pages;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id) && Objects.equals(title, book.title) && Objects.equals(isbn, book.isbn) &&
                Objects.equals(genre, book.genre) && Objects.equals(thumbnailUrl, book.thumbnailUrl) &&
                Objects.equals(description, book.description) && Objects.equals(publisher, book.publisher) &&
                Objects.equals(pages, book.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, isbn, genre, thumbnailUrl, description, publisher, pages);
    }
}
