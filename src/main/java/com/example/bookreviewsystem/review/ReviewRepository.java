package com.example.bookreviewsystem.review;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    @Query(value =
            "SELECT id, ratings, isbn, avg " +
                    "FROM book " +
                    "JOIN " +
                    "(SELECT book_id, ROUND(AVG(rating), 2) AS avg, COUNT(*) ratings FROM review group by book_id) AS statistics " +
                    "ON statistics.book_id = id;",
            nativeQuery = true)
    List<ReviewStatistic> getReviewStatistics();

    List<ReviewEntity> findTop5ByOrderByRatingDescCreatedAtDesc();

    List<ReviewEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    void deleteByIdAndBookEntityIsbn(Long reviewId, String isbn);

    Optional<ReviewEntity> findByIdAndBookEntityIsbn(Long reviewId, String isbn);
}
