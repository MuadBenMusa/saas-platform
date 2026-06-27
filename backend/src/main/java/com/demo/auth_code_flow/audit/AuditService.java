package com.demo.auth_code_flow.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String actorSubject, String action, String entityType, String entityId) {
        auditLogRepository.save(new AuditLog(actorSubject, action, entityType, entityId));
    }
}