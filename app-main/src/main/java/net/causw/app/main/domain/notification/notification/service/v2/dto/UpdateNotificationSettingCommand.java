package net.causw.app.main.domain.notification.notification.service.v2.dto;

import java.util.EnumMap;
import java.util.Map;

import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;

public record UpdateNotificationSettingCommand(
	Boolean likeOnMyPost,
	Boolean commentOnMyPost,
	Boolean replyOnMyComment,
	Boolean ceremonyEnabled,
	Boolean serviceNoticeEnabled
) {

	/**
	 * null이 아닌 필드만 Map으로 변환한다 (부분 업데이트).
	 */
	public Map<UserNotificationSettingKey, Boolean> toSettingMap() {
		Map<UserNotificationSettingKey, Boolean> map = new EnumMap<>(UserNotificationSettingKey.class);
		if (likeOnMyPost != null) {
			map.put(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST, likeOnMyPost);
		}
		if (commentOnMyPost != null) {
			map.put(UserNotificationSettingKey.COMMUNITY_COMMENT_ON_MY_POST, commentOnMyPost);
		}
		if (replyOnMyComment != null) {
			map.put(UserNotificationSettingKey.COMMUNITY_REPLY_ON_MY_COMMENT, replyOnMyComment);
		}
		if (ceremonyEnabled != null) {
			map.put(UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED, ceremonyEnabled);
		}
		if (serviceNoticeEnabled != null) {
			map.put(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED, serviceNoticeEnabled);
		}
		return map;
	}
}
