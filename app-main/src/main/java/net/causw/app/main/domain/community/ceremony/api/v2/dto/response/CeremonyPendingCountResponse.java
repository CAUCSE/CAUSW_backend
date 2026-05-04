package net.causw.app.main.domain.community.ceremony.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미처리 경조사 수 응답")
public record CeremonyPendingCountResponse(
	@Schema(description = "대기 상태인 경조사 수", example = "5") long pendingCount) {
}
