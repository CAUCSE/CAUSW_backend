package net.causw.app.main.domain.notification.notification.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeType {

	// v1
	POST("게시물 알림 - v1"),
	COMMENT("댓글 알림 - v1"),
	BOARD("보드 알림 - v1"),
	ADMISSION("재학인증 알림 - v1"),

	CEREMONY("경조사 알림 - v1, v2 공통"),

	// v2
	COMMUNITY("커뮤니티 알림 - v2"),
	SYSTEM("시스템 알림 - v2"),
	OFFICIAL("공식 알림 - v2");

	private final String type;
}
