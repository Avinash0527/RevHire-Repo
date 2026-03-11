package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.response.AuthResponse;
import com.example.revhirehiringplatform.dto.request.ForgotPasswordRequest;
import com.example.revhirehiringplatform.dto.request.ResetPasswordRequest;
import com.example.revhirehiringplatform.dto.request.TokenRefreshRequest;
import com.example.revhirehiringplatform.dto.request.UpdatePasswordRequest;
import com.example.revhirehiringplatform.dto.request.UserLoginRequest;
import com.example.revhirehiringplatform.dto.request.UserRegistrationRequest;
import com.example.revhirehiringplatform.model.RefreshToken;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.security.JwtUtil;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.service.AuthService;
import com.example.revhirehiringplatform.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationDto) {
        try {
            User user = authService.registerUser(registrationDto);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        try {
            authService.generateAndSendOtp(email);
            return ResponseEntity.ok("OTP sent to " + email);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (authService.verifyOtp(email, otp)) {
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> getLoginInfo() {
        return ResponseEntity.ok("To login, please send a POST request with email and password.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserLoginRequest loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();


            if (User.Role.ADMIN.equals(userDetails.getRole())) {
                return ResponseEntity.status(403).body("Administrators must use the admin login portal.");
            }

            String jwt = jwtUtil.generateJwtToken(authentication);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .refreshToken(refreshToken.getToken())
                    .name(authService.getUserById(userDetails.getId()).getName())
                    .email(userDetails.getEmail())
                    .phone(userDetails.getPhone())
                    .role(userDetails.getRole())
                    .id(userDetails.getId())
                    .build());
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> authenticateAdmin(@Valid @RequestBody UserLoginRequest loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();


            if (!User.Role.ADMIN.equals(userDetails.getRole())) {
                return ResponseEntity.status(403).body("This login is for administrators only.");
            }

            String jwt = jwtUtil.generateJwtToken(authentication);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            log.info("Admin login successful for user: {}", userDetails.getEmail());

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .refreshToken(refreshToken.getToken())
                    .name(authService.getUserById(userDetails.getId()).getName())
                    .email(userDetails.getEmail())
                    .phone(userDetails.getPhone())
                    .role(userDetails.getRole())
                    .id(userDetails.getId())
                    .build());
        } catch (Exception e) {
            log.error("Admin authentication failed: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid admin email or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        java.util.Optional<RefreshToken> tokenOpt = refreshTokenService.findByToken(requestRefreshToken);

        if (tokenOpt.isPresent()) {
            RefreshToken token = refreshTokenService.verifyExpiration(tokenOpt.get());
            User user = token.getUser();
            String jwt = jwtUtil.generateTokenFromEmail(user.getEmail());
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .refreshToken(requestRefreshToken)
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole())
                    .id(user.getId())
                    .build());
        } else {
            return ResponseEntity.status(403).body("Refresh token is not in database!");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            Long userId = ((UserDetailsImpl) principal).getId();
            refreshTokenService.deleteByUserId(userId);
        }
        return ResponseEntity.ok("Log out successful");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok("If an account exists with that email, a reset token has been generated.");
        } catch (Exception e) {

            return ResponseEntity.ok("If an account exists with that email, a reset token has been generated.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest request,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            User user = authService.getUserById(userDetails.getId());
            authService.updatePassword(user, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}