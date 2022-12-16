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
    private String genre;
    private String thumbnailUrl;
    private String description;
    private String publisher;
    private Integer pages;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookEntity bookEntity = (BookEntity) o;
        return Objects.equals(id, bookEntity.id) && Objects.equals(title, bookEntity.title) && Objects.equals(isbn, bookEntity.isbn) &&
                Objects.equals(genre, bookEntity.genre) && Objects.equals(thumbnailUrl, bookEntity.thumbnailUrl) &&
                Objects.equals(description, bookEntity.description) && Objects.equals(publisher, bookEntity.publisher) &&
                Objects.equals(pages, bookEntity.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, isbn, genre, thumbnailUrl, description, publisher, pages);
    }
}
