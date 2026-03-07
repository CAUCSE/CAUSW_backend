package net.causw.app.main.shared.dto;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 프로필 이미지 정보를 담는 재사용 가능한 DTO.
 *
 * <p>Service 레이어에서 프로필 이미지 정보를 전달할 때 사용되며,
 * 응답 DTO에서는 {@code profileImageType}과 {@code profileImageUrl} 필드로 flat하게 노출됩니다.</p>
 *
 * <h3>비즈니스 규칙</h3>
 * <ul>
 *   <li>CUSTOM 타입인 경우에만 URL이 포함되며, 나머지 타입은 URL이 null</li>
 *   <li>추방(DROP) / 탈퇴(DELETED, INACTIVE) 유저는 GHOST 타입, URL null</li>
 *   <li>차단된 유저는 GHOST 타입, URL null (비식별)</li>
 * </ul>
 *
 * @param profileImageType 프로필 이미지 타입
 * @param profileImageUrl  커스텀 프로필 이미지 URL (CUSTOM이 아닌 경우 null)
 */
@Schema(description = "프로필 이미지 정보")
public record ProfileImageDto(

	@Schema(description = "프로필 이미지 타입", example = "CUSTOM") ProfileImageType profileImageType,

	@Schema(description = "프로필 이미지 URL (CUSTOM 타입인 경우에만 값이 존재, 나머지는 null)", example = "https://cdn.example.com/profile/image.png", nullable = true) String profileImageUrl) {

	/** GHOST 프로필 (추방/탈퇴/차단 유저용) */
	public static final ProfileImageDto GHOST = new ProfileImageDto(ProfileImageType.GHOST, null);

	/**
	 * User 엔티티로부터 ProfileImageDto를 생성합니다.
	 * 추방/탈퇴 유저는 GHOST 처리됩니다.
	 */
	public static ProfileImageDto from(User user) {
		if (user == null) {
			return GHOST;
		}
		if (isInactiveUser(user)) {
			return GHOST;
		}
		return of(user.getProfileImageType(), user.getProfileUrl());
	}

	/**
	 * 차단된 유저의 프로필 이미지를 GHOST로 변환합니다.
	 */
	public static ProfileImageDto forBlockedUser() {
		return GHOST;
	}

	/**
	 * 익명 게시글/댓글에서 사용 (프로필 정보 비노출).
	 */
	public static ProfileImageDto anonymous() {
		return null;
	}

	/**
	 * 타입과 URL로 직접 생성합니다.
	 * CUSTOM이 아닌 경우 URL을 null로 강제합니다.
	 */
	public static ProfileImageDto of(ProfileImageType type, String url) {
		if (type == null) {
			return GHOST;
		}
		if (type != ProfileImageType.CUSTOM) {
			return new ProfileImageDto(type, null);
		}
		return new ProfileImageDto(type, url);
	}

	private static boolean isInactiveUser(User user) {
		UserState state = user.getState();
		return state == UserState.DROP || user.isDeleted();
	}
}
