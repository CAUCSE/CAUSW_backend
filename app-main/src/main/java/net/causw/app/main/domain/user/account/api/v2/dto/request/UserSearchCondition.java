package net.causw.app.main.domain.user.account.api.v2.dto.request;

import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserSearchCondition(
	@Schema(description = "유저 상태") UserState userState,
	@Schema(description = "유저 역할") Role userRole,
	@Schema(description = "유저 검색을 위한 keyword (이메일, 이름 like 검색)") String keyword) {
}
