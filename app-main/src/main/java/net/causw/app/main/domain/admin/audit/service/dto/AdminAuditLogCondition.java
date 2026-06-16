package net.causw.app.main.domain.admin.audit.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;

/**
 * 관리자 감사 로그 조회 조건.
 *
 * <p>API 요청을 서비스 계층에서 검증/정규화한 뒤 QueryRepository로 전달하는 DTO입니다.
 * {@code actionType}은 카테고리별 확장을 위해 특정 도메인 enum이 아닌 문자열로 유지합니다.</p>
 *
 * @param from       생성 시각 포함 하한
 * @param to         생성 시각 포함 상한
 * @param category   감사 로그 카테고리
 * @param actionType 카테고리별 액션 타입
 * @param keyword    수행자 또는 대상자의 이메일, 이름, 학번 검색 키워드
 */
public record AdminAuditLogCondition(
	LocalDateTime from,
	LocalDateTime to,
	AdminAuditLogCategory category,
	String actionType,
	String keyword) {
}
