package net.causw.app.main.domain.model.enums.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeType {
	POST("게시물 알림"),
	COMMENT("댓글 알림"), CEREMONY("경조사 알림"), BOARD("보드 알림");

	private final String type;
}
