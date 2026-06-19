package net.causw.app.main.domain.admin.audit.service;

import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCondition;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;
import net.causw.app.main.domain.admin.audit.service.implementation.AdminAuditLogReader;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuditLogService {

	private static final Set<String> USER_ACTION_TYPES = Set.of("DROP", "RESTORE", "ROLE_CHANGE");
	private static final Set<String> LOCKER_ACTION_TYPES = Set.of(
		"ASSIGN",
		"EXTEND",
		"RELEASE",
		"ENABLE",
		"DISABLE",
		"RELEASE_EXPIRED");
	private static final Set<String> ACADEMIC_ACTION_TYPES = Set.of(
		"ADMISSION_ACCEPT",
		"ADMISSION_REJECT",
		"ACADEMIC_RECORD_ACCEPT",
		"ACADEMIC_RECORD_REJECT");
	private static final Map<AdminAuditLogCategory, Set<String>> ACTION_TYPES_BY_CATEGORY = Map.of(
		AdminAuditLogCategory.USER, USER_ACTION_TYPES,
		AdminAuditLogCategory.LOCKER, LOCKER_ACTION_TYPES,
		AdminAuditLogCategory.ACADEMIC, ACADEMIC_ACTION_TYPES);

	private final AdminAuditLogReader adminAuditLogReader;

	/**
	 * 관리자 감사 로그 검색 조건을 검증하고 목록을 조회
	 * @param condition 관리자 감사 로그 검색 조건
	 * @param pageable 페이지 요청
	 * @return 관리자 감사 로그 목록 페이지
	 */
	public Page<AdminAuditLogItem> getAuditLogs(AdminAuditLogCondition condition, Pageable pageable) {
		validateDateRange(condition);
		String actionTypeValue = normalize(condition.actionType());
		String actionType = normalizeActionType(condition.category(), actionTypeValue);
		return adminAuditLogReader.findAuditLogs(toCondition(condition, actionType), pageable);
	}

	private void validateDateRange(AdminAuditLogCondition condition) {
		if (condition.from() != null && condition.to() != null && condition.from().isAfter(condition.to())) {
			throw GlobalErrorCode.BAD_REQUEST.toBaseException();
		}
	}

	private AdminAuditLogCondition toCondition(AdminAuditLogCondition condition, String actionType) {
		return new AdminAuditLogCondition(
			condition.from(),
			condition.to(),
			condition.category(),
			actionType,
			normalize(condition.keyword()));
	}

	private String normalizeActionType(AdminAuditLogCategory category, String actionType) {
		if (actionType == null) {
			return null;
		}
		String normalizedActionType = actionType.toUpperCase();
		if (isAllowedActionType(category, normalizedActionType)) {
			return normalizedActionType;
		}
		throw GlobalErrorCode.BAD_REQUEST.toBaseException();
	}

	private boolean isAllowedActionType(AdminAuditLogCategory category, String actionType) {
		if (category != null) {
			return ACTION_TYPES_BY_CATEGORY.getOrDefault(category, Set.of()).contains(actionType);
		}
		return ACTION_TYPES_BY_CATEGORY.values().stream()
			.anyMatch(actionTypes -> actionTypes.contains(actionType));
	}

	private String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
