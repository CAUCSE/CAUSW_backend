package net.causw.app.main.domain.user.account.api.v2.dto.request;

import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "프로필 이미지 변경 요청")
public record UpdateProfileImageRequest(

	@NotNull(message = "프로필 이미지 타입은 필수입니다.") @Schema(description = "프로필 이미지 타입. 기본 이미지(MALE_1, MALE_2, FEMALE_1, FEMALE_2)를 선택하거나 CUSTOM(커스텀 이미지)을 설정합니다.", example = "MALE_1") ProfileImageType profileImageType) {
}
