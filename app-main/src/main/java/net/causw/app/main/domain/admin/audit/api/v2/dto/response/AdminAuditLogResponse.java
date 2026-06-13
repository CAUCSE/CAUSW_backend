package net.causw.app.main.domain.admin.audit.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 감사 로그 응답")
public record AdminAuditLogResponse(
	@Schema(description = "감사 로그 ID") String id,
	@Schema(description = "감사 로그 카테고리") AdminAuditLogCategory category,
	@Schema(description = "액션 타입") String actionType,
	@Schema(description = "액션 설명") String actionDescription,
	@Schema(description = "수행자") AuditActorResponse actor,
	@Schema(description = "대상") AuditTargetResponse target,
	@Schema(description = "요약 문구") String summary,
	@Schema(description = "추가 메타데이터") Map<String, Object> metadata,
	@Schema(description = "생성 시각") LocalDateTime createdAt) {
}
