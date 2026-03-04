package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 복구 결과 응답")
public record UserRestoreResponse(
	@Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	String id,

	@Schema(description = "변경된 사용자 상태", example = "ACTIVE")
	UserState state,

	@Schema(description = "변경된 사용자 권한 목록", example = "[\"COMMON\"]")
	List<String> roles) {
}
