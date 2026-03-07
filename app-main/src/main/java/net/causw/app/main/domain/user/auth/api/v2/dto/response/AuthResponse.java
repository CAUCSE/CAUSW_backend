package net.causw.app.main.domain.user.auth.api.v2.dto.response;

import net.causw.app.main.shared.dto.ProfileImageDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "인증 응답")
public record AuthResponse(
	@Schema(description = "액세스 토큰") String accessToken,
	@Schema(description = "사용자 프로필: 이름", example = "홍길동") String name,
	@Schema(description = "사용자 프로필: 이메일", example = "user@cau.ac.kr") String email,
	@Schema(description = "사용자 프로필: 프로필 이미지 정보") ProfileImageDto profileImage) {
}
