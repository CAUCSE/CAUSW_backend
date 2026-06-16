package net.causw.app.main.domain.admin.audit.service.dto;

import java.util.Map;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;

/**
 * 관리자 감사 로그 생성 명령.
 *
 * <p>각 도메인 서비스 또는 도메인 전용 로그 writer가 공통 감사 로그 writer에 전달하는 DTO입니다.
 * 수행자와 대상 정보는 로그 발생 시점의 스냅샷으로 넘겨야 합니다.</p>
 *
 * @param category          감사 로그 카테고리
 * @param actionType        카테고리별 액션 타입
 * @param actionDescription 액션 타입 표시명
 * @param actorUserId       수행자 사용자 ID
 * @param actorEmail        로그 발생 시점의 수행자 이메일
 * @param actorName         로그 발생 시점의 수행자 이름
 * @param actorStudentId    로그 발생 시점의 수행자 학번
 * @param targetType        대상 타입
 * @param targetId          대상 ID
 * @param targetEmail       로그 발생 시점의 대상 이메일
 * @param targetName        로그 발생 시점의 대상 이름
 * @param targetStudentId   로그 발생 시점의 대상 학번
 * @param summary           목록과 상세에서 사용할 요약 문구
 * @param metadata          카테고리별 상세 변경 정보
 */
public record AdminAuditLogCreateCommand(
	AdminAuditLogCategory category,
	String actionType,
	String actionDescription,
	String actorUserId,
	String actorEmail,
	String actorName,
	String actorStudentId,
	String targetType,
	String targetId,
	String targetEmail,
	String targetName,
	String targetStudentId,
	String summary,
	Map<String, Object> metadata) {
}
