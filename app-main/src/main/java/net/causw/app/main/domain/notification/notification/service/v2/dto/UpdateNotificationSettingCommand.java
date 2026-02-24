package net.causw.app.main.domain.notification.notification.service.v2.dto;

import java.util.EnumMap;
import java.util.Map;

import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;

/**
 * 알림 설정 부분 업데이트 커맨드 DTO.
 * null 필드는 업데이트 대상에서 제외된다.
 *
 * @param likeOnMyPost        내 게시글 좋아요 알림 활성화 여부 (null이면 변경 안 함)
 * @param commentOnMyPost     내 게시글 댓글 알림 활성화 여부 (null이면 변경 안 함)
 * @param replyOnMyComment    내 댓글 대댓글 알림 활성화 여부 (null이면 변경 안 함)
 * @param ceremonyEnabled     경조사 알림 활성화 여부 (null이면 변경 안 함)
 * @param serviceNoticeEnabled 서비스 공지 알림 활성화 여부 (null이면 변경 안 함)
 */
public record UpdateNotificationSettingCommand(
	Boolean likeOnMyPost,
	Boolean commentOnMyPost,
	Boolean replyOnMyComment,
	Boolean ceremonyEnabled,
	Boolean serviceNoticeEnabled) {

	/**
	 * null이 아닌 필드만 Map으로 변환한다 (부분 업데이트).
	 */
	public UserNotificationSettingMap toSettingMap() {
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
		return UserNotificationSettingMap.ofPartial(map);
	}
}
