package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	
	@Query("SELECT u FROM User u WHERE u.email=?1")
	Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.verificationToken = ?1")
    Optional<User> findByVerificationToken(String token);
    
    User findByResetToken(String resetToken);
}