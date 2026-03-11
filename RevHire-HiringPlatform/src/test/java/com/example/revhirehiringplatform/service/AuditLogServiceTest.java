package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.model.AuditLog;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogAction() {
        User user = new User();
        user.setId(1L);
        auditLogService.logAction("JobPost", 1L, "CREATE", "old", "new", user);
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}

