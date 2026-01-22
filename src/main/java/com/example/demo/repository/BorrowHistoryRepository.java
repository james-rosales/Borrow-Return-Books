package com.example.demo.repository;

import com.example.demo.model.BorrowHistory;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowHistoryRepository extends JpaRepository<BorrowHistory, Long> {

    List<BorrowHistory> findByBorrowedByAndReturnDateIsNull(String borrowedBy);

    List<BorrowHistory> findAllByOrderByBorrowDateDesc();
    
    List<BorrowHistory> findByReturnDateIsNotNullOrderByReturnDateDesc();

    @Query("SELECT DISTINCT bh.borrowedBy FROM BorrowHistory bh WHERE bh.borrowedBy IS NOT NULL")
    List<String> findDistinctBorrowedBy();

    List<BorrowHistory> findByReturnDateIsNull();
    
    @Query("SELECT bh.book.title, COUNT(bh.book.id) AS borrowCount " +
            "FROM BorrowHistory bh " +
            "GROUP BY bh.book.title " +
            "ORDER BY borrowCount DESC")
     List<Object[]> findMostBorrowedBooks();
     
     @Modifying
     @Transactional
     @Query("DELETE FROM BorrowHistory bh WHERE bh.returnDate IS NOT NULL")
     void deleteByReturnDateIsNotNull();
}