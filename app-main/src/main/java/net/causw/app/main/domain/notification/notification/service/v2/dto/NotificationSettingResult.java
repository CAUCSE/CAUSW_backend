package net.causw.app.main.domain.notification.notification.service.v2.dto;

import java.util.List;

import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;

public record NotificationSettingResult(
	CommunitySettings community,
	CeremonySettings ceremony,
	ServiceSettings service,
	List<OfficialBoardSetting> officialBoards
) {

	public record CommunitySettings(
		boolean likeOnMyPost,
		boolean commentOnMyPost,
		boolean replyOnMyComment
	) {}

	public record CeremonySettings(boolean enabled) {}

	public record ServiceSettings(boolean noticeEnabled) {}

	public static NotificationSettingResult from(
		UserNotificationSettingMap settingMap,
		List<OfficialBoardSetting> officialBoards
	) {
		return new NotificationSettingResult(
			new CommunitySettings(
				settingMap.get(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST),
				settingMap.get(UserNotificationSettingKey.COMMUNITY_COMMENT_ON_MY_POST),
				settingMap.get(UserNotificationSettingKey.COMMUNITY_REPLY_ON_MY_COMMENT)
			),
			new CeremonySettings(settingMap.get(UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED)),
			new ServiceSettings(settingMap.get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)),
			officialBoards
		);
	}
}
