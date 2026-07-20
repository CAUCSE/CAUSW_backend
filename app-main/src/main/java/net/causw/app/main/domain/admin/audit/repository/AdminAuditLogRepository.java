package net.causw.app.main.domain.admin.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.admin.audit.entity.AdminAuditLog;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, String> {}
