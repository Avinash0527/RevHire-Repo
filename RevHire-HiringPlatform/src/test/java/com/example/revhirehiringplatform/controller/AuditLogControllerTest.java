package com.example.revhirehiringplatform.controller;
import com.example.revhirehiringplatform.dto.response.AuditLogResponse;
import com.example.revhirehiringplatform.model.AuditLog;
import com.example.revhirehiringplatform.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class AuditLogControllerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogController auditLogController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPublicTest() {
        ResponseEntity<String> response = auditLogController.publicTest();
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetAllLogs() {
        when(auditLogRepository.findAll()).thenReturn(Collections.singletonList(new AuditLog()));
        ResponseEntity<List<AuditLogResponse>> response = auditLogController.getAllLogs();
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetLogById() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(new AuditLog()));
        ResponseEntity<AuditLogResponse> response = auditLogController.getLogById(1L);
        assertEquals(200, response.getStatusCode().value());

        when(auditLogRepository.findById(2L)).thenReturn(Optional.empty());
        ResponseEntity<AuditLogResponse> notFound = auditLogController.getLogById(2L);
        assertEquals(404, notFound.getStatusCode().value());
    }

    @Test
    void testGetLogsByEntityType() {
        when(auditLogRepository.findByEntityType("JobPost")).thenReturn(Collections.singletonList(new AuditLog()));
        ResponseEntity<List<AuditLogResponse>> response = auditLogController.getLogsByEntityType("JobPost");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetLogsByEntity() {
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc("JobPost", 1L))
                .thenReturn(Collections.singletonList(new AuditLog()));
        ResponseEntity<List<AuditLogResponse>> response = auditLogController.getLogsByEntity("JobPost", 1L);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetLogsByUser() {
        when(auditLogRepository.findByChangedBy_IdOrderByChangedAtDesc(1L))
                .thenReturn(Collections.singletonList(new AuditLog()));
        ResponseEntity<List<AuditLogResponse>> response = auditLogController.getLogsByUser(1L);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCleanupLogs() {
        ResponseEntity<Void> response = auditLogController.cleanupLogs(10);
        assertEquals(200, response.getStatusCode().value());
    }
}
