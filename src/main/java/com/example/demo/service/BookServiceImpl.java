package com.example.demo.service;

import com.example.demo.model.Book;
import com.example.demo.model.Author; // Import Author
import com.example.demo.model.BorrowHistory; // Import BorrowHistory
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BorrowHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // Import ArrayList
import java.util.Comparator; // Import Comparator
import java.util.List;
import java.util.Map; // Import Map
import java.util.Optional;
import java.util.stream.Collectors; // Import Collectors
import java.util.Arrays; // Import Arrays for debugging System.out.println

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowHistoryRepository borrowHistoryRepository;

    @Autowired
    private AuthorService authorService; 

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<Book> getBooksByStatus(String status) {
        return bookRepository.findByStatus(status);
    }

    @Override
    public List<Book> getBooksByBorrowerName(String borrowerName) {
        return bookRepository.findByBorrowerName(borrowerName);
    }

    @Override
    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    @Override
    public List<Book> findAvailableBooksWithQuantityGreaterThan(int quantity) {
        return bookRepository.findByStatusAndQuantityGreaterThan("Available", quantity);
    }

    @Override
    public List<Book> getBorrowedBooksByBorrowerName(String borrowerName) {
        return bookRepository.findByStatusAndBorrowerName("Borrowed", borrowerName);
    }

    @Override
    public List<Book> getBorrowedBooks() {
        
        return bookRepository.findByBorrowerNameIsNotNull(); // assuming a new method in BookRepository
    }

    @Override
    public Optional<Book> getBookByTitleAndBorrowerName(String title, String borrowerName) {
        return bookRepository.findByTitleAndBorrowerName(title, borrowerName);
    }

    @Override
    public Optional<Book> getBookByTitleAndAuthorName(String title, String authorName) {
        Author author = null;
        List<Author> authors = authorService.searchAuthors(authorName);
        if (!authors.isEmpty()) {
            author = authors.get(0);
            return bookRepository.findByTitleAndAuthor(title, author);
        }
        return Optional.empty();
    }

    @Override
    public List<Book> getBooksByAuthorId(Long authorId) {
        Author author = authorService.getAuthorById(authorId);
        if (author != null) {
            return bookRepository.findByAuthor(author);
        }
        return List.of();
    }

    @Override
    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    @Override
    public boolean deleteBook(Long id) {
        try {
            bookRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Object[]> getMostBorrowedBooks() {
        List<BorrowHistory> allBorrowHistory = borrowHistoryRepository.findAll();

        Map<Book, Long> borrowCounts = allBorrowHistory.stream()
            .collect(Collectors.groupingBy(BorrowHistory::getBook, Collectors.counting()));

        List<Map.Entry<Book, Long>> sortedBorrowCounts = borrowCounts.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toList());

        List<Object[]> mostBorrowedBooksData = new ArrayList<>();
        for (Map.Entry<Book, Long> entry : sortedBorrowCounts) {
            Book book = entry.getKey();
            Long count = entry.getValue();

            Object[] bookData = new Object[3];
            bookData[0] = book.getTitle(); 
            bookData[1] = (book.getAuthor() != null) ? book.getAuthor().getName() : "N/A"; 
            bookData[2] = count; 

            System.out.println("DEBUG: Book data array size: " + bookData.length + ", Content: " + Arrays.toString(bookData));
            
            mostBorrowedBooksData.add(bookData);
        }
        
        return mostBorrowedBooksData;
    }

    @Override
    public List<Book> getBooksByIds(List<Long> ids) {
        return bookRepository.findAllById(ids);
    }
    
    @Override
    public List<String> getAllDistinctCategories() {
        return bookRepository.findDistinctCategory();
    }
}