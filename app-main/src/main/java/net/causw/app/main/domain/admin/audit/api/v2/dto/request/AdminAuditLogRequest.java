package net.causw.app.main.domain.admin.audit.api.v2.dto.request;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 감사 로그 목록 조회 요청")
public record AdminAuditLogRequest(
	@Schema(description = "조회 시작 시각") LocalDateTime from,
	@Schema(description = "조회 종료 시각") LocalDateTime to,
	@Schema(description = "감사 로그 카테고리", example = "USER") AdminAuditLogCategory category,
	@Schema(description = "액션 타입", example = "DROP") String actionType,
	@Schema(description = "관리자 또는 대상 사용자 이메일 키워드") String keyword) {
}
