package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rating_history")
public class RatingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_history_id", nullable = false)
    private BorrowHistory borrowHistory;
    
    @Column(nullable = false)
    private int rating; // 1-5 stars
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;
    
    // Constructors
    public RatingHistory() {}
    
    public RatingHistory(User user, Book book, BorrowHistory borrowHistory, int rating, String comment, LocalDate reviewDate) {
        this.user = user;
        this.book = book;
        this.borrowHistory = borrowHistory;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    
    public BorrowHistory getBorrowHistory() { return borrowHistory; }
    public void setBorrowHistory(BorrowHistory borrowHistory) { this.borrowHistory = borrowHistory; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
}