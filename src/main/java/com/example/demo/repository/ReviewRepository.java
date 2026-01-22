package com.example.demo.repository;

import com.example.demo.model.Review;
import com.example.demo.model.User;
import com.example.demo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByUser(User user);
    
    List<Review> findByBook(Book book);
    
    List<Review> findByBookId(Long bookId);
    
    Optional<Review> findByUserAndBook(User user, Book book);
    
    List<Review> findByRating(Integer rating);
    
    List<Review> findAllByOrderByReviewDateDesc();
}
