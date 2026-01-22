package com.example.demo.controller;

import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class VerificationController {

    @Autowired
    private UserService userService;
    
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        boolean isVerified = userService.verifyUser(token);
        if (isVerified) {
            String responseBody = """
                ✅ Account verified successfully! You can now log in. <br>
                <a href='http://localhost:8080/login'>Click here to log in</a>
            """;
            return ResponseEntity.ok().header("Content-Type", "text/html").body(responseBody);
        } else {
            String errorResponse = """
                ❌ Invalid or expired verification token. <br>
            """;
            return ResponseEntity.badRequest().header("Content-Type", "text/html").body(errorResponse);
        }
    }
}
