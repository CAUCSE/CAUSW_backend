package net.causw.app.main.domain.admin.audit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.audit.api.v2.dto.request.AdminAuditLogRequest;
import net.causw.app.main.domain.admin.audit.repository.AdminAuditLogQueryRepository;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCondition;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuditLogService {

	private final AdminAuditLogQueryRepository adminAuditLogQueryRepository;

	public Page<AdminAuditLogItem> getAuditLogs(AdminAuditLogRequest request, Pageable pageable) {
		validateDateRange(request);
		return adminAuditLogQueryRepository.findAuditLogs(toCondition(request), pageable);
	}

	private void validateDateRange(AdminAuditLogRequest request) {
		if (request.from() != null && request.to() != null && request.from().isAfter(request.to())) {
			throw GlobalErrorCode.BAD_REQUEST.toBaseException();
		}
	}

	private AdminAuditLogCondition toCondition(AdminAuditLogRequest request) {
		return new AdminAuditLogCondition(
			request.from(),
			request.to(),
			request.category(),
			normalize(request.actionType()),
			normalize(request.keyword()));
	}

	private String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
