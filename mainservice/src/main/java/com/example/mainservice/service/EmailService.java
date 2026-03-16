package com.example.mainservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending password reset emails.
 * Currently logs the reset link for development purposes.
 * To enable actual email sending, configure Spring Mail in application.properties
 * and uncomment the email sending code below.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final JavaMailSender javaMailSender;

    /**
     * Get the password reset link
     * @param resetToken The password reset token
     * @param role The user role (DOCTOR, PATIENT, ADMIN) to determine redirect destination
     */
    public String getResetLink(String resetToken, String role) {
        return frontendUrl + "/reset-password?token=" + resetToken + "&role=" + role.toUpperCase();
    }

    /**
     * Send password reset email to user
     * @param email User's email address
     * @param resetToken Password reset token
     * @param username User's username
     * @param role User's role (DOCTOR, PATIENT, ADMIN)
     */
    public void sendPasswordResetEmail(String email, String resetToken, String username, String role) {
        String resetLink = getResetLink(resetToken, role);
        
        String subject = "Password Reset Request";
        String body = buildPasswordResetEmailBody(username, resetLink, resetToken);
        
        // Log for development
        log.info("=== PASSWORD RESET EMAIL ===");
        log.info("To: {}", email);
        log.info("Subject: {}", subject);
        log.info("Reset Link: {}", resetLink);
        log.info("Reset Token: {}", resetToken);
        log.info("Body:\n{}", body);
        log.info("===========================");
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendLoginOtpEmail(String email, String otp, String displayName) {
        String subject = "Your Login Verification Code";
        String body = buildLoginOtpEmailBody(displayName, otp);

        // Log for development
        log.info("=== LOGIN OTP EMAIL ===");
        log.info("To: {}", email);
        log.info("Subject: {}", subject);
        log.info("OTP: {}", otp);
        log.info("Body:\n{}", body);
        log.info("=======================");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info("Login OTP email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send login OTP email to: {}", email, e);
            throw new RuntimeException("Failed to send login OTP email", e);
        }
    }

    public void sendPasswordResetOtpEmail(String email, String otp, String displayName) {
        String subject = "Your Password Reset Code";
        String body = buildPasswordResetOtpEmailBody(displayName, otp);

        log.info("=== PASSWORD RESET OTP EMAIL ===");
        log.info("To: {}", email);
        log.info("Subject: {}", subject);
        log.info("OTP: {}", otp);
        log.info("Body:\n{}", body);
        log.info("===============================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info("Password reset OTP email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset OTP email", e);
        }
    }

    private String buildPasswordResetEmailBody(String username, String resetLink, String resetToken) {
        return String.format(
            "Hello %s,\n\n" +
            "You have requested to reset your password.\n\n" +
            "Click the following link to reset your password:\n" +
            "%s\n\n" +
            "Or use this token: %s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Healthcare System",
            username, resetLink, resetToken
        );
    }

    private String buildLoginOtpEmailBody(String name, String otp) {
        String safeName = (name == null || name.trim().isEmpty()) ? "User" : name.trim();
        return String.format(
                "Hello %s,\n\n" +
                        "Use the following code to complete your login:\n\n" +
                        "%s\n\n" +
                        "This code will expire in 10 minutes.\n\n" +
                        "If you did not attempt to login, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Healthcare System",
                safeName, otp
        );
    }

    private String buildPasswordResetOtpEmailBody(String name, String otp) {
        String safeName = (name == null || name.trim().isEmpty()) ? "User" : name.trim();
        return String.format(
                "Hello %s,\n\n" +
                        "Use the following code to reset your password:\n\n" +
                        "%s\n\n" +
                        "This code will expire in 10 minutes.\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Healthcare System",
                safeName, otp
        );
    }

    /**
     * Send doctor account credentials email
     * @param email Doctor's email address
     * @param name Doctor's name
     * @param username Doctor's username
     * @param password Doctor's password
     */
    public void sendDoctorCredentialsEmail(String email, String name, String username, String password) {
        String subject = "Your Doctor Account Credentials";
        String body = buildDoctorCredentialsEmailBody(name, username, password);

        log.info("=== DOCTOR CREDENTIALS EMAIL ===");
        log.info("To: {}", email);
        log.info("Subject: {}", subject);
        log.info("Username: {}", username);
        log.info("Body:\n{}", body);
        log.info("===============================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info("Doctor credentials email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send doctor credentials email to: {}", email, e);
            throw new RuntimeException("Failed to send doctor credentials email", e);
        }
    }

    private String buildDoctorCredentialsEmailBody(String name, String username, String password) {
        String safeName = (name == null || name.trim().isEmpty()) ? "Doctor" : name.trim();
        return String.format(
                "Hello %s,\n\n" +
                        "Your doctor account has been created by the administrator.\n\n" +
                        "Please use the following credentials to login:\n\n" +
                        "Username: %s\n" +
                        "Password: %s\n\n" +
                        "Please login at: %s/doctorLogin\n\n" +
                        "For security reasons, we recommend changing your password after your first login.\n\n" +
                        "If you have any questions, please contact the administrator.\n\n" +
                        "Best regards,\n" +
                        "Healthcare System",
                safeName, username, password, frontendUrl
        );
    }
}
