package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 45, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, name="first_name", length = 45)
    private String firstName;

    @Column(nullable = false, name="last_name", length = 45)
    private String lastName;

    @Column(nullable = false, length = 10)
    private String role;

    @Column(name = "verification_token", length = 64, unique = true)
    private String verificationToken;

    @Column(name = "is_verified")
    private boolean isVerified;

    @Column(name = "status")
    private String status;

    @Column(name = "reset_token", length = 64, unique = true)
    private String resetToken;

    @Column(name = "reset_token_expiry") 
    private Long resetTokenExpiry;


    // 📌 Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean isVerified) { this.isVerified = isVerified; }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Long getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(Long resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }
}