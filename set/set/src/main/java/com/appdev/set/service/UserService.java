package com.appdev.set.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appdev.set.model.User;
import com.appdev.set.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    // Save user
    public void save(User user) {
        repo.save(user);
    }

    // Find user by email
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
        
    }
}
