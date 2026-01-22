package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailSenderService;
import com.example.demo.service.UserService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Ito ay para sa @ResponseBody at @RequestParam

import org.springframework.stereotype.Controller; // Keep this
import org.springframework.ui.Model; // Keep this
import org.mindrot.jbcrypt.BCrypt; // Import BCrypt

@Controller
public class ResetPasswordController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailSenderService emailService;

    @GetMapping("/resetpassword") 
    public String showResetPasswordRequestForm() {
        return "resetpassword"; 
    }

    @PostMapping("/auth/reset-password-request") 
    @ResponseBody 
    public ResponseEntity<String> processResetPasswordRequest(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        }

        String resetToken = UUID.randomUUID().toString();
        Long expiryTime = System.currentTimeMillis() + (60 * 60 * 1000); 
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(expiryTime);
        userRepository.save(user);

        String resetLink = "http://localhost:8080/LIBRARY/auth/reset-password-confirm?token=" + resetToken; 

        try {
            emailService.sendEmail(user.getEmail(), "Password Reset Request",
                    "To reset your password, click the link below:\n" + resetLink);
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage()); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to send reset email. Please try again.");
        }
    }
    
    @GetMapping("/auth/reset-password-confirm") 
    public String showResetPasswordConfirmPage(@RequestParam("token") String token, Model model) {
        User user = userRepository.findByResetToken(token); 

        if (user == null || user.getResetTokenExpiry() == null || user.getResetTokenExpiry() < System.currentTimeMillis()) {
            model.addAttribute("error", "Invalid or expired reset token.");
            return "redirect:/LIBRARY/login"; 
        }

        model.addAttribute("token", token); 
        return "resetpasswordconfirmation"; 
    }

    // 📌 Process New Password Submission
    @PostMapping("/auth/process-reset") 
    public String processResetPassword(@RequestParam("token") String token, 
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        User user = userRepository.findByResetToken(token); 

        if (user == null || user.getResetTokenExpiry() == null || user.getResetTokenExpiry() < System.currentTimeMillis()) {
            model.addAttribute("error", "Invalid or expired reset token.");
            return "redirect:/login"; 
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            model.addAttribute("token", token); 
            return "resetpasswordconfirmation";
        }

        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        user.setPassword(hashedPassword);
        user.setResetToken(null); 
        user.setResetTokenExpiry(null); 
        userRepository.save(user);

        model.addAttribute("message", "Password reset successfully! You can now log in with your new password.");
        return "redirect:/login?resetSuccess=true"; 
    }
}