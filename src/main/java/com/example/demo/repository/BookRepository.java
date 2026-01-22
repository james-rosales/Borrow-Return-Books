package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Book;
import com.example.demo.model.Author; 

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByStatus(String status);

    List<Book> findByBorrowerName(String borrowerName);

    List<Book> findByCategory(String category);

    List<Book> findByStatusAndQuantityGreaterThan(String status, int quantity);

    List<Book> findByStatusAndBorrowerName(String status, String borrowerName);

    List<Book> findByBorrowerNameIsNotNullAndBorrowerName(String borrowerName);

    Optional<Book> findByTitleAndBorrowerName(String title, String borrowerName);

    Optional<Book> findByTitleAndAuthor(String title, Author author);

    List<Book> findByAuthor(Author author);
    
 
    @Query("SELECT DISTINCT b.borrowerName FROM Book b WHERE b.borrowerName IS NOT NULL")
    List<String> findDistinctBorrowerNames();
    
 
    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL")
    List<String> findDistinctCategory();
    
    List<Book> findByBorrowerNameIsNotNull(); 
    
    List<Book> findTop6ByStatusOrderByTitleAsc(String status);
    
 // Find books by quantity
    List<Book> findByQuantity(int quantity);

    // Find books with quantity greater than 0 (available books)
    List<Book> findByQuantityGreaterThan(int quantity);

    // Find books with quantity equal to 0 (out of stock books)
    @Query("SELECT b FROM Book b WHERE b.quantity = 0")
    List<Book> findOutOfStockBooks();
}