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

	/**
	 * 관리자 감사 로그 검색 조건을 검증하고 목록을 조회
	 * @param request 관리자 감사 로그 검색 조건
	 * @param pageable 페이지 요청
	 * @return 관리자 감사 로그 목록 페이지
	 */
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
