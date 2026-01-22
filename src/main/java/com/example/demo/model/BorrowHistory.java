package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "borrow_history")
public class BorrowHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double fine;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrowed_by", nullable = false)
    private String borrowedBy; 

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "borrow_time", nullable = false)
    private LocalTime borrowTime;
    
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "return_date") 
    private LocalDate returnDate;

    @Column(name = "return_time") 
    private LocalTime returnTime;

    @Column(name = "returned_by") 
    private String returnedBy;

    public BorrowHistory() {}

    public BorrowHistory(Book book, String borrowedBy, LocalDate borrowDate, LocalTime borrowTime, LocalDate dueDate) { 
        this.book = book;
        this.borrowedBy = borrowedBy;
        this.borrowDate = borrowDate;
        this.borrowTime = borrowTime;
        this.dueDate = dueDate;
        this.fine = 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    
    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public String getBorrowedBy() { return borrowedBy; }
    public void setBorrowedBy(String borrowedBy) { this.borrowedBy = borrowedBy; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalTime getBorrowTime() { return borrowTime; }
    public void setBorrowTime(LocalTime borrowTime) { this.borrowTime = borrowTime; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public LocalTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalTime returnTime) { this.returnTime = returnTime; }

    public String getReturnedBy() { return returnedBy; }
    public void setReturnedBy(String returnedBy) { this.returnedBy = returnedBy; }
    
    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
}