package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 추방 결과 응답")
public record UserDropResponse(
	@Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000") String id,

	@Schema(description = "변경된 사용자 상태", example = "DROP") UserState state,

	@Schema(description = "변경된 사용자 권한 목록", example = "[\"NONE\"]") List<String> roles,

	@Schema(description = "변경된 추방 사유", example = "운영 정책 위반", nullable = true) String dropReason) {
}
