package com.example.bookreviewsystem.book;

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
@Table(name = "book")
public class BookEntity {
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
    private String author;
    private String genre;
    private String thumbnailUrl;
    private String description;
    private String publisher;
    private Long pages;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookEntity that = (BookEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(isbn, that.isbn) && Objects.equals(author, that.author) && Objects.equals(genre, that.genre) && Objects.equals(thumbnailUrl, that.thumbnailUrl) && Objects.equals(description, that.description) && Objects.equals(publisher, that.publisher) && Objects.equals(pages, that.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, isbn, author, genre, thumbnailUrl, description, publisher, pages);
    }
}
