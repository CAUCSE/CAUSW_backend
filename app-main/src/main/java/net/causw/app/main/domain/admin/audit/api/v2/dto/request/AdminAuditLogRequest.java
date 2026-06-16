package net.causw.app.main.domain.admin.audit.api.v2.dto.request;

import java.time.LocalDateTime;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 감사 로그 목록 조회 요청")
public record AdminAuditLogRequest(

	@Schema(description = "조회 시작 시각 (createdAt 포함 하한)", example = "2026-06-13T00:00:00") LocalDateTime from,

	@Schema(description = "조회 종료 시각 (createdAt 포함 상한)", example = "2026-06-13T23:59:59") LocalDateTime to,

	@Schema(description = "감사 로그 카테고리", example = "USER") AdminAuditLogCategory category,

	@Schema(description = "카테고리별 액션 타입. USER 카테고리는 DROP, RESTORE, ROLE_CHANGE를 지원", example = "DROP") String actionType,

	@Schema(description = "수행자 또는 대상 사용자의 이메일, 이름, 학번 검색 키워드", example = "admin@causw.net") String keyword) {
}
