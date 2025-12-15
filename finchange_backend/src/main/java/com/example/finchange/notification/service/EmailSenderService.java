package com.example.finchange.notification.service;

public interface EmailSenderService {

    void sendWelcomeEmail(String toEmail, String firstName, String tempPassword);

    void sendPasswordResetEmail(String toEmail, String token);
}