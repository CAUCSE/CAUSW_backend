package net.causw.app.main.domain.community.post.enums;

import lombok.Getter;

@Getter
public enum PostCategory {
	RECRUIT("채용"),
	ACADEMIC("학사"),
	EVENT_LECTURE("행사/특강"),
	EXTERNAL_ACTIVITY("대외활동"),
	RESEARCH("연구"),
	// 자동 분류 대상이 아니며 관리자만 지정한다.
	ETC("기타");

	private final String label;

	PostCategory(String label) {
		this.label = label;
	}
}
