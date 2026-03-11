package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.model.AuditLog;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    @Transactional(propagation = Propagation.REQUIRED)
    public void logAction(String entityType, Long entityId, String action, String oldValue, String newValue,
                          User changedBy) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(newValue);
            auditLog.setChangedBy(changedBy);

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} on {} ID: {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }
}