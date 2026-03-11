package com.example.revhirehiringplatform.controller;


import com.example.revhirehiringplatform.dto.response.ApplicationNoteResponse;
import com.example.revhirehiringplatform.model.ApplicationNotes;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationNotesRepository;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/notes")
@RequiredArgsConstructor
@Slf4j
public class ApplicationNotesController {

    private static final String UNAUTHORIZED_MSG = "Unauthorized";

    private final ApplicationNotesRepository notesRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        return userRepository.findById(userDetails.getId()).orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> getNotesForApplication(@PathVariable("applicationId") Long applicationId,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
        }


        com.example.revhirehiringplatform.model.Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Application not found");
        }
        if (!application.getJobPost().getCreatedBy().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Unauthorized to view these notes");
        }

        List<ApplicationNoteResponse> notes = notesRepository.findByApplicationId(applicationId).stream()
                .map(this::mapToDto)
                .toList();

        return ResponseEntity.ok(notes);
    }

    @PostMapping
    public ResponseEntity<?> addNote(@PathVariable("applicationId") Long applicationId,
                                     @RequestBody String noteText,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
        }
        com.example.revhirehiringplatform.model.Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Application not found");
        }
        ApplicationNotes note = new ApplicationNotes();
        note.setApplication(application);
        note.setNoteText(noteText);
        note.setCreatedBy(user);
        return ResponseEntity.ok(mapToDto(notesRepository.save(note)));
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable("noteId") Long noteId) {
        return notesRepository.findById(noteId)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(@PathVariable("noteId") Long noteId,
                                        @RequestBody String noteText,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        return notesRepository.findById(noteId).map(note -> {
            if (!note.getCreatedBy().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
            }
            note.setNoteText(noteText);
            return ResponseEntity.ok(mapToDto(notesRepository.save(note)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable("noteId") Long noteId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        return notesRepository.findById(noteId).map(note -> {
            if (!note.getCreatedBy().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(UNAUTHORIZED_MSG);
            }
            notesRepository.delete(note);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private ApplicationNoteResponse mapToDto(ApplicationNotes note) {
        ApplicationNoteResponse dto = new ApplicationNoteResponse();
        dto.setId(note.getId());
        dto.setNoteText(note.getNoteText());
        dto.setCreatedByUserId(note.getCreatedBy().getId());
        dto.setCreatedByUserName(note.getCreatedBy().getName());
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }
}