package com.example.demo.service;

import com.example.demo.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> getAllBooks();
    Optional<Book> getBookById(Long id);
    List<Book> getBooksByStatus(String status);
    List<Book> getBooksByBorrowerName(String borrowerName);
    List<Book> getBooksByCategory(String category);
    List<Book> findAvailableBooksWithQuantityGreaterThan(int quantity);
    List<Book> getBorrowedBooksByBorrowerName(String borrowerName);
    List<Book> getBorrowedBooks();
    Optional<Book> getBookByTitleAndBorrowerName(String title, String borrowerName);
    Optional<Book> getBookByTitleAndAuthorName(String title, String authorName);
    List<Book> getBooksByAuthorId(Long authorId);
    void saveBook(Book book);
    boolean deleteBook(Long id);
    List<Object[]> getMostBorrowedBooks();
    List<Book> getBooksByIds(List<Long> ids);
    List<String> getAllDistinctCategories();
}