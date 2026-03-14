package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 권한 변경 결과 응답")
public record UserRoleUpdateResponse(
	@Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000") String id,

	@Schema(description = "변경된 사용자 권한 목록", example = "[\"ADMIN\"]") List<String> roles) {
}
