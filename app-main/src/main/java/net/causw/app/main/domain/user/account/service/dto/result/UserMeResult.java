package net.causw.app.main.domain.user.account.service.dto.result;

import java.util.List;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record UserMeResult(
	String id,
	String name,
	String nickname,
	String profileImageUrl,
	Integer admissionYear,
	UserState state,
	List<String> roles,
	String job) {

	public static UserMeResult from(User user, UserInfo userInfo) {
		return new UserMeResult(
			user.getId(),
			user.getName(),
			user.getNickname(),
			extractProfileImageUrl(user),
			user.getAdmissionYear(),
			user.getState(),
			user.getRoles().stream().map(Enum::name).toList(),
			userInfo != null ? userInfo.getJob() : null);
	}

	private static String extractProfileImageUrl(User user) {
		if (user.getUserProfileImage() == null) {
			return null;
		}
		var uuidFile = user.getUserProfileImage().getUuidFile();
		return uuidFile != null ? uuidFile.getFileUrl() : null;
	}
}
