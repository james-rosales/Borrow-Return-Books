package com.example.demo.repository;

import com.example.demo.model.Book;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByUser(User user);
    
    List<Reservation> findByBook(Book book);
    
    List<Reservation> findByBookAndNotificationSentFalse(Book book);
    
    List<Reservation> findByUserAndIsAvailableTrue(User user);
    
    boolean existsByUserAndBook(User user, Book book);
    
    List<Reservation> findByUserAndCancelledDateIsNull(User user);

    List<Reservation> findByUserAndCompletedDateIsNullAndCancelledDateIsNull(User user);

    List<Reservation> findByUserAndIsAvailableFalseAndCancelledDateIsNull(User user);
    
    List<Reservation> findByUserAndCompletedDateIsNotNull(User user);
    
    List<Reservation> findByUserAndCancelledDateIsNotNull(User user);
    
    List<Reservation> findByUserAndIsAvailableTrueAndCompletedDateIsNullAndCancelledDateIsNull(User user);

    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND r.isAvailable = true AND r.completedDate IS NULL AND r.cancelledDate IS NULL")
    List<Reservation> findAvailableReservationsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user = :user AND r.book = :book AND r.completedDate IS NULL AND r.cancelledDate IS NULL")
    long countByUserAndBookAndCompletedDateIsNullAndCancelledDateIsNull(@Param("user") User user, @Param("book") Book book);
    
    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND r.book = :book AND r.completedDate IS NULL AND r.cancelledDate IS NULL")
    List<Reservation> findByUserAndBookAndCompletedDateIsNullAndCancelledDateIsNull(@Param("user") User user, @Param("book") Book book);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user = :user AND r.book = :book")
    long countByUserAndBook(@Param("user") User user, @Param("book") Book book);
    
    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND r.book = :book ORDER BY r.reservationDate DESC")
    List<Reservation> findByUserAndBookOrderByReservationDateDesc(@Param("user") User user, @Param("book") Book book);
    
    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND r.isAvailable = false AND r.completedDate IS NULL AND r.cancelledDate IS NULL")
    List<Reservation> findPendingReservationsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book = :book AND r.isAvailable = false AND r.completedDate IS NULL AND r.cancelledDate IS NULL")
    long countPendingReservationsByBook(@Param("book") Book book);
}
