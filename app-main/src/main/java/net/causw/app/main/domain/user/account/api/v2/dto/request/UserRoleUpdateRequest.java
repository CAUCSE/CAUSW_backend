package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import net.causw.app.main.domain.user.account.enums.user.Role;

@Schema(description = "회원 권한 변경 요청")
public record UserRoleUpdateRequest(
	@Schema(description = "현재 권한", example = "COMMON", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "현재 권한을 입력해 주세요.")
	Role currentRole,

	@Schema(description = "변경할 권한", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "변경할 권한을 입력해 주세요.")
	Role newRole) {
}
