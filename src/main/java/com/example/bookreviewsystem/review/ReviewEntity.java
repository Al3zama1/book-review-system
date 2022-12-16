package com.example.bookreviewsystem.review;

import com.example.bookreviewsystem.user.UserEntity;
import com.example.bookreviewsystem.book.BookEntity;
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
@Table(name = "review")
public class ReviewEntity {
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
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id"
    )
    private UserEntity userEntity;
    @ManyToOne
    @JoinColumn(
            name = "book_id",
            referencedColumnName = "id"
    )
    private BookEntity bookEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewEntity reviewEntity = (ReviewEntity) o;
        return Objects.equals(id, reviewEntity.id) && Objects.equals(title, reviewEntity.title) &&
                Objects.equals(content, reviewEntity.content) && Objects.equals(rating, reviewEntity.rating) &&
                Objects.equals(createdAt, reviewEntity.createdAt) && Objects.equals(userEntity, reviewEntity.userEntity) &&
                Objects.equals(bookEntity, reviewEntity.bookEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, rating, createdAt, userEntity, bookEntity);
    }
}
