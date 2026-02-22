package net.causw.app.main.domain.notification.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserNotificationSettingKey {

	COMMUNITY_LIKE_ON_MY_POST("내 글에 좋아요", true),
	COMMUNITY_COMMENT_ON_MY_POST("내 글에 댓글", true),
	COMMUNITY_REPLY_ON_MY_COMMENT("내 댓글의 대댓글", true),
	CEREMONY_NOTIFICATION_ENABLED("경조사 알림", true),
	SERVICE_NOTICE_ENABLED("서비스 공지 알림", false);

	private final String label;
	private final boolean defaultEnabled;
}
