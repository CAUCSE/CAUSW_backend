package net.causw.app.main.domain.user.account.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record TotalUserCountResponse(
	@Schema(description = "전체 회원 수", example = "28377171") Long totalUserCount) {
}