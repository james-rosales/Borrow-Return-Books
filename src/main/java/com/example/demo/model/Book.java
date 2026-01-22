package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false) 
    private Author author;

    @Column(nullable = false)
    private String status = "Available";
    private int quantity = 0;
    private String category;

    @Column(name = "borrower_name")
    private String borrowerName;

    @Column(name = "borrowed_date")
    private LocalDate borrowedDate;

    @Column(name = "borrowed_time")
    private LocalTime borrowedTime;

    // Constructors
    public Book() {}

    public Book(String title, Author author, String status) { 
        this.title = title;
        this.author = author;
        this.status = status;
    }

    // Overloaded constructor with all fields including coverImageUrl and description
    public Book(String title, Author author, String status, int quantity, String category, String borrowerName, LocalDate borrowedDate, LocalTime borrowedTime, String coverImageUrl, String description) {
        this.title = title;
        this.author = author;
        this.status = status;
        this.quantity = quantity;
        this.category = category;
        this.borrowerName = borrowerName;
        this.borrowedDate = borrowedDate;
        this.borrowedTime = borrowedTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public LocalDate getBorrowedDate() {
        return borrowedDate;
    }

    public void setBorrowedDate(LocalDate borrowedDate) {
        this.borrowedDate = borrowedDate;
    }

    public LocalTime getBorrowedTime() {
        return borrowedTime;
    }

    public void setBorrowedTime(LocalTime borrowedTime) {
        this.borrowedTime = borrowedTime;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}