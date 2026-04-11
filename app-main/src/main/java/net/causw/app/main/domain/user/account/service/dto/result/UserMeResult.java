package net.causw.app.main.domain.user.account.service.dto.result;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.shared.dto.ProfileImageDto;

public record UserMeResult(
	String id,
	String name,
	String nickname,
	ProfileImageDto profileImage,
	Integer admissionYear,
	String job) {

	public static UserMeResult from(User user, UserInfo userInfo, UserProfileImage userProfileImage) {
		return new UserMeResult(
			user.getId(),
			user.getName(),
			user.getNickname(),
			ProfileImageDto.from(user, userProfileImage),
			user.getAdmissionYear(),
			userInfo != null ? userInfo.getJob() : null);
	}
}
