package com.example.revhirehiringplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.revhirehiringplatform.model.EmployerProfile;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.EmployerProfileRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EmployerProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @InjectMocks
    private EmployerProfileController employerProfileController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User owner;
    private User otherUser;
    private EmployerProfile profile;
    private UserDetailsImpl ownerDetails;
    private UserDetailsImpl otherDetails;
    private Authentication ownerAuth;
    private Authentication otherAuth;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employerProfileController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@test.com");
        owner.setRole(User.Role.EMPLOYER);

        otherUser = new User();
        otherUser.setId(2L);

        profile = new EmployerProfile();
        profile.setId(10L);
        profile.setUser(owner);
        profile.setDesignation("HR Manager");

        ownerDetails = new UserDetailsImpl(1L, "owner", "owner@test.com", "1234",
                "pass", User.Role.EMPLOYER, Collections.emptyList());
        otherDetails = new UserDetailsImpl(2L, "other", "other@test.com", "5678",
                "pass", User.Role.EMPLOYER, Collections.emptyList());

        ownerAuth = new UsernamePasswordAuthenticationToken(ownerDetails, null, ownerDetails.getAuthorities());
        otherAuth = new UsernamePasswordAuthenticationToken(otherDetails, null, otherDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(ownerAuth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetAllProfiles() throws Exception {
        when(employerProfileRepository.findAll()).thenReturn(List.of(profile));

        mockMvc.perform(get("/api/employer-profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }


    @Test
    void testGetProfileById_Found() throws Exception {
        when(employerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/employer-profiles/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void testGetProfileById_NotFound() throws Exception {
        when(employerProfileRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employer-profiles/99"))
                .andExpect(status().isNotFound());
    }



    @Test
    void testUpdateProfile_Success() throws Exception {
        EmployerProfile updated = new EmployerProfile();
        updated.setDesignation("Director");

        when(employerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenReturn(profile);

        mockMvc.perform(put("/api/employer-profiles/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .principal(ownerAuth))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateProfile_Unauthorized() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(otherAuth);
        EmployerProfile updated = new EmployerProfile();
        updated.setDesignation("Director");

        when(employerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        mockMvc.perform(put("/api/employer-profiles/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .principal(otherAuth))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateProfile_NotFound() throws Exception {
        EmployerProfile updated = new EmployerProfile();
        updated.setDesignation("Director");

        when(employerProfileRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/employer-profiles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .principal(ownerAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProfile_Success() throws Exception {
        when(employerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        mockMvc.perform(delete("/api/employer-profiles/10")
                        .principal(ownerAuth))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteProfile_Unauthorized() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(otherAuth);
        when(employerProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        mockMvc.perform(delete("/api/employer-profiles/10")
                        .principal(otherAuth))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteProfile_NotFound() throws Exception {
        when(employerProfileRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/employer-profiles/99")
                        .principal(ownerAuth))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetProfileByUserId_Found() throws Exception {
        when(employerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/employer-profiles/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void testGetProfileByUserId_NotFound() throws Exception {
        when(employerProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employer-profiles/user/99"))
                .andExpect(status().isNotFound());
    }
}
