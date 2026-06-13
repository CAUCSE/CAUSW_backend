package net.causw.app.main.domain.admin.audit.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감사 로그 수행자 정보")
public record AuditActorResponse(
	@Schema(description = "수행자 사용자 ID") String userId,
	@Schema(description = "수행자 이메일") String email) {
}
