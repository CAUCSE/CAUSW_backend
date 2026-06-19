package net.causw.app.main.domain.admin.audit.service.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.audit.repository.AdminAuditLogQueryRepository;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCondition;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuditLogReader {

	private final AdminAuditLogQueryRepository adminAuditLogQueryRepository;

	public Page<AdminAuditLogItem> findAuditLogs(AdminAuditLogCondition condition, Pageable pageable) {
		return adminAuditLogQueryRepository.findAuditLogs(condition, pageable);
	}
}
