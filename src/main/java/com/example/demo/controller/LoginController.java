package com.example.demo.controller;

import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController 
@RequestMapping("/api") 
public class LoginController {

    private final UserService userService; 

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login") 
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Map<String, Object> response = new HashMap<>(); 

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid request: email and password are required.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); 
        }

        if (userService.authenticate(email, password)) {
            response.put("success", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("success", false);
            response.put("message", "Invalid email or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}