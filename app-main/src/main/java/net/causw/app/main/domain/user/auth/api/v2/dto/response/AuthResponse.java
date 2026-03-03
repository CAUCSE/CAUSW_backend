package net.causw.app.main.domain.user.auth.api.v2.dto.response;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "인증 응답")
public record AuthResponse(
	@Schema(description = "액세스 토큰") String accessToken,
	@Schema(description = "사용자 프로필: 이름", example = "홍길동") String name,
	@Schema(description = "사용자 프로필: 이메일", example = "user@cau.ac.kr") String email,
	@Schema(description = "사용자 프로필: 프로필이미지 url", example = "https://cdn.causw.net/profile/default.png") String profileImgUrl,
	@Schema(description = "사용자 상태", example = "GUEST") UserState userState) {
}
