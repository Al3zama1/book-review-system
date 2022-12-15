package com.example.bookreviewsystem;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Review {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY,
            generator = "review_sequence"
    )
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;
    @Column(nullable = false)
    private Byte rating;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @ManyToOne()
    private User user;
    @ManyToOne
    private Book book;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id) && Objects.equals(title, review.title) &&
                Objects.equals(content, review.content) && Objects.equals(rating, review.rating) &&
                Objects.equals(createdAt, review.createdAt) && Objects.equals(user, review.user) &&
                Objects.equals(book, review.book);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, rating, createdAt, user, book);
    }
}
