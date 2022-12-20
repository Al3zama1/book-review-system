package com.example.bookreviewsystem.review;

import com.example.bookreviewsystem.book.BookEntity;
import com.example.bookreviewsystem.user.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "review")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(
            name = "book_id",
            referencedColumnName = "id"
    )
    private BookEntity bookEntity;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id"
    )
    private UserEntity userEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewEntity that = (ReviewEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) && Objects.equals(rating, that.rating) &&
                Objects.equals(createdAt, that.createdAt) && Objects.equals(bookEntity, that.bookEntity) &&
                Objects.equals(userEntity, that.userEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, rating, createdAt, bookEntity, userEntity);
    }
}
