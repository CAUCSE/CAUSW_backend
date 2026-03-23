package net.causw.app.main.domain.user.account.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import net.causw.app.main.shared.dto.ProfileImageDto;

@Schema(description = "내 정보 조회 응답")
public record UserMeResponse(

	@Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000") String id,

	@Schema(description = "이름", example = "홍길동") String name,

	@Schema(description = "닉네임", example = "푸앙") String nickname,

	@Schema(description = "프로필 이미지 정보") ProfileImageDto profileImage,

	@Schema(description = "입학년도", example = "2020") Integer admissionYear,

	@Schema(description = "직업", example = "개발자", nullable = true) String job) {
}
