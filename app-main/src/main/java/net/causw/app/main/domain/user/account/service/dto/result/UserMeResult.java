package net.causw.app.main.domain.user.account.service.dto.result;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;

public record UserMeResult(
	String id,
	String name,
	String nickname,
	String profileImageUrl,
	Integer admissionYear,
	String job) {

	public static UserMeResult from(User user, UserInfo userInfo) {
		return new UserMeResult(
			user.getId(),
			user.getName(),
			user.getNickname(),
			user.getProfileUrl(),
			user.getAdmissionYear(),
			userInfo != null ? userInfo.getJob() : null);
	}
}
