package net.causw.app.main.domain.admin.audit.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 감사 로그 응답")
public record AdminAuditLogResponse(

	@Schema(description = "감사 로그 ID", example = "11112222-aaaa-3333-bbbb-446655440000") String id,

	@Schema(description = "감사 로그 카테고리", example = "USER") AdminAuditLogCategory category,

	@Schema(description = "카테고리별 액션 타입", example = "DROP") String actionType,

	@Schema(description = "액션 타입 표시명", example = "유저 추방") String actionDescription,

	@Schema(description = "수행자") AuditActorResponse actor,

	@Schema(description = "대상") AuditTargetResponse target,

	@Schema(description = "목록과 상세에서 사용할 요약 문구", example = "admin@causw.net dropped user user@causw.net") String summary,

	@Schema(description = "카테고리별 상세 변경 정보. USER 로그는 이전상태, 이후상태, 이전역할, 이후역할, 사유를 포함") Map<String, Object> metadata,

	@Schema(description = "로그 생성 시각", example = "2026-06-13T10:30:00") LocalDateTime createdAt) {
}
