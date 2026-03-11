package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.UserRegistrationRequest;
import com.example.revhirehiringplatform.model.PasswordResetToken;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.CompanyRepository;
import com.example.revhirehiringplatform.repository.EmployerProfileRepository;
import com.example.revhirehiringplatform.repository.PasswordResetTokenRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.revhirehiringplatform.repository.JobSeekerProfileRepository jobSeekerProfileRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setRole(User.Role.JOB_SEEKER);
    }


    @Test
    void registerUser_Success() {
        UserRegistrationRequest dto = new UserRegistrationRequest();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setRole(User.Role.JOB_SEEKER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = authService.registerUser(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        UserRegistrationRequest dto = new UserRegistrationRequest();
        dto.setEmail("existing@example.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_AsEmployer_CreatesCompanyAndProfile() {
        UserRegistrationRequest dto = new UserRegistrationRequest();
        dto.setName("Employer User");
        dto.setEmail("employer@example.com");
        dto.setPassword("password");
        dto.setRole(User.Role.EMPLOYER);
        dto.setCompanyName("ACME Corp");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        com.example.revhirehiringplatform.model.Company company = new com.example.revhirehiringplatform.model.Company();
        company.setId(10L);
        when(companyRepository.save(any(com.example.revhirehiringplatform.model.Company.class))).thenReturn(company);

        User result = authService.registerUser(dto);

        assertNotNull(result);
        assertEquals(User.Role.EMPLOYER, result.getRole());
        verify(companyRepository, times(1)).save(any(com.example.revhirehiringplatform.model.Company.class));
        verify(employerProfileRepository, times(1)).save(any(com.example.revhirehiringplatform.model.EmployerProfile.class));
    }

    @Test
    void initiatePasswordReset_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendPasswordResetOtpEmail(eq("test@example.com"), anyString());

        assertDoesNotThrow(() -> authService.initiatePasswordReset("test@example.com"));

        verify(passwordResetTokenRepository).findByUser(testUser);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetOtpEmail(eq("test@example.com"), anyString());
    }

    @Test
    void initiatePasswordReset_ExistingToken_RotatesToken() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        PasswordResetToken existing = new PasswordResetToken("old-token", testUser, 30);
        when(passwordResetTokenRepository.findByUser(testUser)).thenReturn(Optional.of(existing));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendPasswordResetOtpEmail(eq("test@example.com"), anyString());

        assertDoesNotThrow(() -> authService.initiatePasswordReset("test@example.com"));

        verify(passwordResetTokenRepository).findByUser(testUser);
        verify(passwordResetTokenRepository).save(existing);
        verify(emailService).sendPasswordResetOtpEmail(eq("test@example.com"), anyString());
        assertNotEquals("old-token", existing.getToken());
        assertTrue(existing.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void initiatePasswordReset_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.initiatePasswordReset("unknown@example.com"));
        verify(passwordResetTokenRepository, never()).save(any());
    }


    @Test
    void resetPassword_Success() {
        PasswordResetToken token = new PasswordResetToken("valid-token", testUser, 30);
        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(anyString())).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(passwordResetTokenRepository).delete(token);

        assertDoesNotThrow(() -> authService.resetPassword("valid-token", "newPass123"));

        verify(userRepository).save(testUser);
        verify(passwordResetTokenRepository).delete(token);
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.resetPassword("bad-token", "newPass"));
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsException() {
        PasswordResetToken expiredToken = new PasswordResetToken("old-token", testUser, 0);
        try {
            java.lang.reflect.Field f = PasswordResetToken.class.getDeclaredField("expiryDate");
            f.setAccessible(true);
            f.set(expiredToken, LocalDateTime.now().minusMinutes(10));
        } catch (Exception e) {
            fail("Could not set expiryDate via reflection: " + e.getMessage());
        }

        when(passwordResetTokenRepository.findByToken("old-token")).thenReturn(Optional.of(expiredToken));
        doNothing().when(passwordResetTokenRepository).delete(expiredToken);

        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword("old-token", "newPass"));
        verify(passwordResetTokenRepository).delete(expiredToken);
    }

    @Test
    void updatePassword_Success() {
        when(passwordEncoder.matches("oldPass", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> authService.updatePassword(testUser, "oldPass", "newPass"));

        verify(userRepository).save(testUser);
        assertEquals("new-encoded", testUser.getPassword());
    }

    @Test
    void updatePassword_WrongOldPassword_ThrowsException() {
        when(passwordEncoder.matches("wrongOld", "encoded-password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.updatePassword(testUser, "wrongOld", "newPass"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_SameAsOld_ThrowsException() {
        when(passwordEncoder.matches("oldPass", "encoded-password")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.updatePassword(testUser, "oldPass", "oldPass"));
        assertEquals("New password must be different from current password", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = authService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.getUserById(99L));
    }
}
