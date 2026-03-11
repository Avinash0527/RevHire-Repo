package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.UserRegistrationRequest;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final com.example.revhirehiringplatform.repository.JobSeekerProfileRepository jobSeekerProfileRepository;
    private final com.example.revhirehiringplatform.repository.CompanyRepository companyRepository;
    private final com.example.revhirehiringplatform.repository.EmployerProfileRepository employerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final com.example.revhirehiringplatform.repository.PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final com.example.revhirehiringplatform.repository.OtpVerificationRepository otpVerificationRepository;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public void generateAndSendOtp(String email) {
        log.info("Generating OTP for: {}", email);
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        com.example.revhirehiringplatform.model.OtpVerification otpVerification = otpVerificationRepository.findByEmail(email)
                .map(existing -> {
                    existing.setOtp(otp);
                    existing.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(5));
                    return existing;
                })
                .orElseGet(() -> new com.example.revhirehiringplatform.model.OtpVerification(email, otp, 5));

        otpVerificationRepository.save(otpVerification);
        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        log.info("Verifying OTP for: {}", email);
        return otpVerificationRepository.findByEmail(email)
                .map(v -> v.getOtp().equals(otp) && v.getExpiryDate().isAfter(java.time.LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        com.example.revhirehiringplatform.model.PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(user)
                .map(existing -> {
                    existing.setToken(token);
                    existing.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(5));
                    return existing;
                })
                .orElseGet(() -> new com.example.revhirehiringplatform.model.PasswordResetToken(token, user, 5));

        passwordResetTokenRepository.save(resetToken);

        try {
            emailService.sendPasswordResetOtpEmail(user.getEmail(), token);
            log.info("Password reset OTP sent for {}", email);
        } catch (Exception ex) {
            log.error("Password reset OTP email failed for {}", email, ex);
            throw new RuntimeException("Failed to send OTP email", ex);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");
        com.example.revhirehiringplatform.model.PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (resetToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
        log.info("Password reset successfully for user: {}", user.getEmail());

        auditLogService.logAction("User", user.getId(), "PASSWORD_RESET", null, "Password reset via token", user);
    }

    @Transactional
    public void updatePassword(User user, String oldPassword, String newPassword) {
        log.info("Updating password for user: {}", user.getEmail());
        boolean matches = passwordEncoder.matches(oldPassword, user.getPassword());
        log.info("Old password match result: {}", matches);
        if (!matches) {
            log.warn("Password update failed: Invalid old password for user {}", user.getEmail());
            throw new IllegalArgumentException("Invalid old password");
        }
        if (oldPassword.equals(newPassword)) {
            log.warn("Password update failed: New password matches current password for user {}", user.getEmail());
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", user.getEmail());

        auditLogService.logAction("User", user.getId(), "PASSWORD_UPDATED", null, "Password updated manually", user);
    }

    public User registerUser(UserRegistrationRequest registrationDto) {
        log.info("Attempting to register user with email: {}", registrationDto.getEmail());
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", registrationDto.getEmail());
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPhone(registrationDto.getPhone());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(registrationDto.getRole());

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == User.Role.JOB_SEEKER) {
            com.example.revhirehiringplatform.model.JobSeekerProfile profile = new com.example.revhirehiringplatform.model.JobSeekerProfile();
            profile.setUser(savedUser);
            profile.setLocation(registrationDto.getLocation());
            profile.setEmploymentStatus(registrationDto.getEmploymentStatus());
            jobSeekerProfileRepository.save(profile);
        } else if (savedUser.getRole() == User.Role.EMPLOYER) {
            com.example.revhirehiringplatform.model.Company company = new com.example.revhirehiringplatform.model.Company();
            company.setName(registrationDto.getCompanyName() != null ? registrationDto.getCompanyName()
                    : savedUser.getName() + "'s Company");
            company.setCreatedBy(savedUser);
            company = companyRepository.save(company);

            com.example.revhirehiringplatform.model.EmployerProfile profile = new com.example.revhirehiringplatform.model.EmployerProfile();
            profile.setUser(savedUser);
            profile.setCompany(company);
            profile.setDesignation("HR / Admin");
            employerProfileRepository.save(profile);
        }

        auditLogService.logAction(
                "User",
                savedUser.getId(),
                "USER_REGISTERED",
                null,
                "Role: " + savedUser.getRole().name(),
                savedUser);

        log.info("User registered successfully: {}", savedUser.getId());

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", savedUser.getEmail(), e);
        }

        return savedUser;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
