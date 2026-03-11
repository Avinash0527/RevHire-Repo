package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.response.AuditLogResponse;
import com.example.revhirehiringplatform.model.AuditLog;
import com.example.revhirehiringplatform.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/public-test")
    public ResponseEntity<String> publicTest() {
        log.info("Public test endpoint reached");
        return ResponseEntity.ok("Public test successful");
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllLogs() {
        log.info("ENTERING AuditLogController.getAllLogs()");
        List<AuditLogResponse> logs = auditLogRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(java.util.stream.Collectors.toList());
        log.info("Found {} audit logs", logs.size());
        return ResponseEntity.ok(logs);
    }

    private AuditLogResponse mapToDto(AuditLog log) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.setId(log.getId());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setAction(log.getAction());
        dto.setOldValue(log.getOldValue());
        dto.setNewValue(log.getNewValue());
        dto.setChangedAt(log.getChangedAt());
        if (log.getChangedBy() != null) {
            dto.setChangedByName(log.getChangedBy().getName());
        } else {
            dto.setChangedByName("System");
        }
        dto.setDetails(log.getAction() + " on " + log.getEntityType() + " #" + log.getEntityId());
        return dto;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getLogById(@PathVariable Long id) {
        log.info("Request received to fetch audit log by ID: {}", id);
        return auditLogRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByEntityType(@PathVariable String entityType) {
        log.info("Request received to fetch audit logs by entity type: {}", entityType);
        return ResponseEntity.ok(auditLogRepository.findByEntityType(entityType).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByEntity(@PathVariable String entityType,
                                                                  @PathVariable Long entityId) {
        return ResponseEntity
                .ok(auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId).stream()
                        .map(this::mapToDto)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogRepository.findByChangedBy_IdOrderByChangedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupLogs(@RequestParam(required = false) Integer daysAgo) {

        return ResponseEntity.ok().build();
    }
}