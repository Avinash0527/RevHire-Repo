package com.example.revhirehiringplatform.controller;


import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationNotesRepository;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationNotesController.class)
@AutoConfigureMockMvc
public class ApplicationNotesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationNotesRepository notesRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private UserRepository userRepository;

    private User employer;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        employer = new User();
        employer.setId(1L);
        employer.setRole(User.Role.EMPLOYER);
        userDetails = UserDetailsImpl.build(employer);
    }

    @Test
    @WithMockUser
    void testGetNotesForApplication() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));

        com.example.revhirehiringplatform.model.Application app = new com.example.revhirehiringplatform.model.Application();
        com.example.revhirehiringplatform.model.JobPost job = new com.example.revhirehiringplatform.model.JobPost();
        job.setCreatedBy(employer);
        app.setJobPost(job);
        when(applicationRepository.findById(any())).thenReturn(Optional.of(app));

        mockMvc.perform(get("/api/applications/1/notes")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testAddNote() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        com.example.revhirehiringplatform.model.Application app = new com.example.revhirehiringplatform.model.Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.of(app));
        when(notesRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(post("/api/applications/1/notes")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Test Note"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetNoteById() throws Exception {
        com.example.revhirehiringplatform.model.ApplicationNotes note = new com.example.revhirehiringplatform.model.ApplicationNotes();
        note.setId(1L);
        note.setCreatedBy(employer);
        when(notesRepository.findById(any())).thenReturn(Optional.of(note));

        mockMvc.perform(get("/api/applications/1/notes/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUpdateNote() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        com.example.revhirehiringplatform.model.ApplicationNotes note = new com.example.revhirehiringplatform.model.ApplicationNotes();
        note.setId(1L);
        note.setCreatedBy(employer);
        when(notesRepository.findById(any())).thenReturn(Optional.of(note));
        when(notesRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(put("/api/applications/1/notes/1")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Updated Note"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeleteNote() throws Exception {
        when(userRepository.findById(any())).thenReturn(Optional.of(employer));
        com.example.revhirehiringplatform.model.ApplicationNotes note = new com.example.revhirehiringplatform.model.ApplicationNotes();
        note.setId(1L);
        note.setCreatedBy(employer);
        when(notesRepository.findById(any())).thenReturn(Optional.of(note));

        mockMvc.perform(delete("/api/applications/1/notes/1")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }
}
