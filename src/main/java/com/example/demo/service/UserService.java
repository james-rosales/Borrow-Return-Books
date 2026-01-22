package com.example.demo.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException; // Import for catching mail exceptions
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.PasswordHasher;

@Service
public class UserService {

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    public void save(User user) {
        userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.orElse(null);
    }

    public boolean sendResetPasswordEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            String resetLink = "http://localhost:8080/LIBRARY/resetpasswordconfirmation";
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Reset Your Password");
            message.setText("Click the link to reset your password: " + resetLink);
            try { 
                mailSender.send(message);
                return true;
            } catch (MailException e) {
                System.err.println("Failed to send reset password email to " + email + ". Error: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);
        return token;
    }

    @Async
    public void sendVerificationEmail(String email, String token, String siteURL) {
        // We use the dynamic 'siteURL' passed from the controller
        String verifyLink = siteURL + "/user/verify?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify Your Email");
        message.setText("Click the link to verify your account: " + verifyLink);
        
        try { 
            mailSender.send(message);
            System.out.println("Verification email sent to: " + email);
        } catch (MailException e) {
            System.err.println("Failed to send verification email to " + email + ". Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean verifyUser(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setVerified(true);
            user.setVerificationToken(null);
            user.setStatus("VERIFIED");
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered.");
        }

        String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        String token = generateVerificationToken(user);
        user.setVerified(false);
        user.setStatus("Pending");

        String verificationLink = "http://localhost:8080/LIBRARY/user/verify?token=" + token;

        emailSenderService.sendEmail(
            user.getEmail(),
            "Verify your account",
            "Click this link to verify your account: " + verificationLink
        );
    }

    public boolean authenticate(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String storedHashedPassword = user.getPassword();
            return PasswordHasher.checkPassword(password, storedHashedPassword);
        }
        return false;
    }
}