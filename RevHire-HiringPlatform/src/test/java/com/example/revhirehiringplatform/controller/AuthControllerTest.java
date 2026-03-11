package com.example.revhirehiringplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

        private MockMvc mockMvc;

        @Mock
        private AuthService authService;

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private JwtUtil jwtUtil;

        @Mock
        private RefreshTokenService refreshTokenService;

        @InjectMocks
        private AuthController authController;

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(authController)
                        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                        .build();
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        @Test
        void testLoginInfo() throws Exception {
                mockMvc.perform(get("/api/auth/login"))
                        .andExpect(status().isOk())
                        .andExpect(content().string(
                                "To login, please send a POST request with email and password."));
        }

        @Test
        void testAuthenticateUser() throws Exception {
                UserLoginRequest loginDto = new UserLoginRequest();
                loginDto.setEmail("test@example.com");
                loginDto.setPassword("password");

                UserDetailsImpl userDetails = new UserDetailsImpl(
                        1L,
                        "test@example.com",
                        "test@example.com",
                        "+1987654321", // Phone number added here
                        "password",
                        User.Role.JOB_SEEKER,
                        Collections.emptyList());

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenReturn(authentication);

                when(jwtUtil.generateJwtToken(authentication)).thenReturn("dummy-jwt-token");

                RefreshToken mockRefreshToken = new RefreshToken();
                mockRefreshToken.setToken("dummy-refresh-token");
                when(refreshTokenService.createRefreshToken(1L)).thenReturn(mockRefreshToken);


                User loginUser = new User();
                loginUser.setId(1L);
                loginUser.setName("Test User");
                when(authService.getUserById(1L)).thenReturn(loginUser);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDto)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").value("dummy-jwt-token"))
                        .andExpect(jsonPath("$.phone").value("+1987654321"))
                        .andExpect(jsonPath("$.email").value("test@example.com"));
        }

        @Test
        void testAuthenticateUser_Failure() throws Exception {
                UserLoginRequest loginDto = new UserLoginRequest();
                loginDto.setEmail("test@example.com");
                loginDto.setPassword("wrong");

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .thenThrow(new RuntimeException("Bad credentials"));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDto)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(content().string("Invalid email or password"));
        }

        @Test
        void testRegisterUser() throws Exception {
                UserRegistrationRequest regDto = new UserRegistrationRequest();
                regDto.setName("Jane Doe");
                regDto.setEmail("jane@doe.com");
                regDto.setPhone("+111222333");
                regDto.setPassword("securepass");
                regDto.setRole(User.Role.JOB_SEEKER);

                User savedUser = new User();
                savedUser.setId(10L);
                savedUser.setName("Jane Doe");
                savedUser.setEmail("jane@doe.com");
                savedUser.setPhone("+111222333");
                savedUser.setRole(User.Role.JOB_SEEKER);

                when(authService.registerUser(any(UserRegistrationRequest.class))).thenReturn(savedUser);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(regDto)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(10))
                        .andExpect(jsonPath("$.name").value("Jane Doe"))
                        .andExpect(jsonPath("$.phone").value("+111222333"))
                        .andExpect(jsonPath("$.role").value("JOB_SEEKER"));
        }

        @Test
        void testRegisterUser_Failure() throws Exception {
                UserRegistrationRequest regDto = new UserRegistrationRequest();
                regDto.setName("Jane Doe");
                regDto.setEmail("jane@doe.com");
                regDto.setPhone("+111222333");
                regDto.setPassword("securepass");
                regDto.setRole(User.Role.JOB_SEEKER);

                when(authService.registerUser(any(UserRegistrationRequest.class)))
                        .thenThrow(new RuntimeException("Email already exists"));

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(regDto)))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string("Email already exists"));
        }

        @Test
        void testRefreshToken_Success() throws Exception {
                TokenRefreshRequest request = new TokenRefreshRequest();
                request.setRefreshToken("dummy-refresh-token");

                RefreshToken token = new RefreshToken();
                token.setToken("dummy-refresh-token");
                User user = new User();
                user.setId(1L);
                user.setEmail("test@test.com");
                user.setName("Test User");
                user.setPhone("1234567890");
                user.setRole(User.Role.JOB_SEEKER);
                token.setUser(user);

                when(refreshTokenService.findByToken("dummy-refresh-token")).thenReturn(Optional.of(token));
                when(refreshTokenService.verifyExpiration(token)).thenReturn(token);
                when(jwtUtil.generateTokenFromEmail("test@test.com")).thenReturn("new-jwt-token");

                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").value("new-jwt-token"))
                        .andExpect(jsonPath("$.refreshToken").value("dummy-refresh-token"))
                        .andExpect(jsonPath("$.email").value("test@test.com"));
        }

        @Test
        void testRefreshToken_NotFound() throws Exception {
                TokenRefreshRequest request = new TokenRefreshRequest();
                request.setRefreshToken("dummy-refresh-token");

                when(refreshTokenService.findByToken("dummy-refresh-token")).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isForbidden())
                        .andExpect(content().string("Refresh token is not in database!"));
        }

        @Test
        void testLogoutUser() throws Exception {
                UserDetailsImpl userDetails = new UserDetailsImpl(
                        1L,
                        "test@example.com",
                        "test@example.com",
                        "+1987654321",
                        "password",
                        User.Role.JOB_SEEKER,
                        Collections.emptyList());

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                when(refreshTokenService.deleteByUserId(1L)).thenReturn(1);

                mockMvc.perform(post("/api/auth/logout")
                                .principal(authentication))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Log out successful"));
        }

        @Test
        void testForgotPassword_Success() throws Exception {
                ForgotPasswordRequest request = new ForgotPasswordRequest();
                request.setEmail("test@test.com");

                doNothing().when(authService).initiatePasswordReset("test@test.com");

                mockMvc.perform(post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().string(
                                "If an account exists with that email, a reset token has been generated."));
        }

        @Test
        void testForgotPassword_Exception() throws Exception {
                ForgotPasswordRequest request = new ForgotPasswordRequest();
                request.setEmail("test@test.com");

                doThrow(new RuntimeException("User not found")).when(authService)
                        .initiatePasswordReset("test@test.com");

                mockMvc.perform(post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().string(
                                "If an account exists with that email, a reset token has been generated."));
        }

        @Test
        void testResetPassword_Success() throws Exception {
                ResetPasswordRequest request = new ResetPasswordRequest();
                request.setToken("reset-token");
                request.setNewPassword("newPass");

                doNothing().when(authService).resetPassword("reset-token", "newPass");

                mockMvc.perform(post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Password reset successfully."));
        }

        @Test
        void testResetPassword_Exception() throws Exception {
                ResetPasswordRequest request = new ResetPasswordRequest();
                request.setToken("reset-token");
                request.setNewPassword("newPass");

                doThrow(new RuntimeException("Invalid token")).when(authService).resetPassword("reset-token",
                        "newPass");

                mockMvc.perform(post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string("Invalid token"));
        }

        @Test
        void testUpdatePassword_Success() throws Exception {
                UpdatePasswordRequest request = new UpdatePasswordRequest();
                request.setOldPassword("oldPass");
                request.setNewPassword("newPass");

                UserDetailsImpl userDetails = new UserDetailsImpl(
                        1L,
                        "test@example.com",
                        "test@example.com",
                        "+1987654321",
                        "password",
                        User.Role.JOB_SEEKER,
                        Collections.emptyList());

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                User mockUser = new User();
                mockUser.setId(1L);

                when(authService.getUserById(1L)).thenReturn(mockUser);
                doNothing().when(authService).updatePassword(mockUser, "oldPass", "newPass");

                mockMvc.perform(post("/api/auth/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(authentication)) // fallback
                        .andExpect(status().isOk())
                        .andExpect(content().string("Password updated successfully."));
        }

        @Test
        void testUpdatePassword_Unauthorized() throws Exception {
                UpdatePasswordRequest request = new UpdatePasswordRequest();
                request.setOldPassword("oldPass");
                request.setNewPassword("newPass");

                mockMvc.perform(post("/api/auth/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(content().string("Unauthorized"));
        }

        @Test
        void testUpdatePassword_Failure() throws Exception {
                UpdatePasswordRequest request = new UpdatePasswordRequest();
                request.setOldPassword("oldPass");
                request.setNewPassword("newPass");

                UserDetailsImpl userDetails = new UserDetailsImpl(
                        1L,
                        "test@example.com",
                        "test@example.com",
                        "+1987654321",
                        "password",
                        User.Role.JOB_SEEKER,
                        Collections.emptyList());

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                User mockUser = new User();
                mockUser.setId(1L);

                when(authService.getUserById(1L)).thenReturn(mockUser);
                doThrow(new RuntimeException("Wrong old password")).when(authService).updatePassword(mockUser,
                        "oldPass", "newPass");

                mockMvc.perform(post("/api/auth/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(authentication)) // fallback
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string("Wrong old password"));
        }

        @Test
        void testUpdatePassword_SameAsOld_Failure() throws Exception {
                UpdatePasswordRequest request = new UpdatePasswordRequest();
                request.setOldPassword("samePass");
                request.setNewPassword("samePass");

                UserDetailsImpl userDetails = new UserDetailsImpl(
                        1L,
                        "test@example.com",
                        "test@example.com",
                        "+1987654321",
                        "password",
                        User.Role.JOB_SEEKER,
                        Collections.emptyList());

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                User mockUser = new User();
                mockUser.setId(1L);

                when(authService.getUserById(1L)).thenReturn(mockUser);
                doThrow(new RuntimeException("New password must be different from current password"))
                        .when(authService).updatePassword(mockUser, "samePass", "samePass");

                mockMvc.perform(post("/api/auth/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(authentication))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string("New password must be different from current password"));
        }
}
