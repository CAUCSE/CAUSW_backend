package net.causw.app.main.domain.user.account.api.v2.dto.response;

import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 정보 응답")
public record ProfileImageResponse(

	@Schema(description = "프로필 이미지 타입", example = "CUSTOM") ProfileImageType profileImageType,

	@Schema(description = "커스텀 프로필 이미지 URL. 기본 이미지인 경우 null입니다.", example = "https://cdn.example.com/profile/image.png", nullable = true) String profileImageUrl) {

	public static ProfileImageResponse of(ProfileImageType profileImageType, String profileImageUrl) {
		return new ProfileImageResponse(profileImageType, profileImageUrl);
	}
}
