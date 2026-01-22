package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException; // Import for catching mail exceptions
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("jamesrosales427@gmail.com"); 
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail + " with subject: '" + subject + "'");
        } catch (MailException e) {
            System.err.println("Failed to send email to " + toEmail + ". Error: " + e.getMessage());
            e.printStackTrace(); 
        }
    }
}