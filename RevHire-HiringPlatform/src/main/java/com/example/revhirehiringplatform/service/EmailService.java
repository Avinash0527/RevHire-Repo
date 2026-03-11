package com.example.revhirehiringplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail);
        }
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        log.info("Email sent to {} with subject: {}", toEmail, subject);
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String body = "We received a request to reset your RevHire password.\n\n"
                + "Use this link to reset your password:\n"
                + resetLink + "\n\n"
                + "This link expires in 30 minutes.\n\n"
                + "If you did not request this, you can ignore this email.";
        sendEmail(toEmail, "RevHire Password Reset", body);
    }

    public void sendApplicationConfirmation(String toEmail, String jobTitle, String companyName) {
        String body = "You have successfully applied for the " + jobTitle + " position at " + companyName + ".\n\n"
                + "You can track your application status in your dashboard.\n\n"
                + "Good luck!\nRevHire Team";
        sendEmail(toEmail, "Application Received: " + jobTitle, body);
    }

    public void sendNewApplicationNotification(String toEmail, String jobTitle, String applicantName) {
        String body = "A new application has been received for your job posting: " + jobTitle + ".\n\n"
                + "Applicant: " + applicantName + "\n\n"
                + "Log in to your employer dashboard to review the application.";
        sendEmail(toEmail, "New Application for " + jobTitle, body);
    }

    public void sendWithdrawalConfirmation(String toEmail, String jobTitle, String companyName) {
        String body = "Your application for the " + jobTitle + " position at " + companyName
                + " has been withdrawn.\n\n"
                + "If you did not intend to withdraw, you can re-apply if the posting is still active.";
        sendEmail(toEmail, "Application Withdrawn: " + jobTitle, body);
    }

    public void sendStatusUpdateNotification(String toEmail, String jobTitle, String status) {
        String body = "Your application status for " + jobTitle + " has been updated to: " + status + ".\n\n"
                + "Please log in to RevHire to see more details.";
        sendEmail(toEmail, "Application Status Update: " + jobTitle, body);
    }

    public void sendWelcomeEmail(String toEmail, String userName) {
        String body = "Hello " + userName + ",\n\n"
                + "Welcome to RevHire! Your account has been created successfully.\n\n"
                + "You can now log in to complete your profile and start "
                + "exploring job opportunities or managing your job postings.\n\n"
                + "Best regards,\nRevHire Team";
        sendEmail(toEmail, "Welcome to RevHire!", body);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        String body = "Hello,\n\n"
                + "Your OTP for RevHire registration is: " + otp + "\n\n"
                + "This OTP is valid for 5 minutes. If you did not request this, please ignore this email.\n\n"
                + "Best regards,\nRevHire Team";
        sendEmail(toEmail, "RevHire Registration OTP", body);
    }

    public void sendPasswordResetOtpEmail(String toEmail, String otp) {
        String body = "We received a request to reset your RevHire password.\n\n"
                + "Use this OTP to reset your password: " + otp + "\n\n"
                + "This OTP expires in 5 minutes.\n\n"
                + "If you did not request this, you can ignore this email.";
        sendEmail(toEmail, "RevHire Password Reset OTP", body);
    }
}
