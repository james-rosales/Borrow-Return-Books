package com.example.demo.repository;

import com.example.demo.model.BorrowHistory;
import com.example.demo.model.RatingHistory;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingHistoryRepository extends JpaRepository<RatingHistory, Long> {
    
    List<RatingHistory> findByUserOrderByReviewDateDesc(User user);
    
    List<RatingHistory> findByUserAndBookIdOrderByReviewDateDesc(User user, Long bookId);
    
    void deleteByBorrowHistory(BorrowHistory borrowHistory);
    
    @Query("SELECT AVG(r.rating) FROM RatingHistory r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(r) FROM RatingHistory r WHERE r.book.id = :bookId")
    Long countRatingsByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT r FROM RatingHistory r WHERE r.user = :user AND r.book.id = :bookId ORDER BY r.reviewDate DESC LIMIT 1")
    RatingHistory findLatestRatingByUserAndBookId(@Param("user") User user, @Param("bookId") Long bookId);
}