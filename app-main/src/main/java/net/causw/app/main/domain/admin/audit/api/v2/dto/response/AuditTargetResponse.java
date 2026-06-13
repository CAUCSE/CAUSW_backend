package net.causw.app.main.domain.admin.audit.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감사 로그 대상 정보")
public record AuditTargetResponse(
	@Schema(description = "대상 타입", example = "USER") String type,
	@Schema(description = "대상 ID") String id,
	@Schema(description = "대상 이메일") String email) {
}
