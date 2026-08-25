package net.causw.app.main.domain.community.post.enums;

import lombok.Getter;

@Getter
public enum PostCategory {
	RECRUIT("채용"),
	ACADEMIC("학사"),
	EVENT_LECTURE("행사/특강"),
	EXTERNAL_ACTIVITY("대외활동"),
	RESEARCH("연구");

	private final String label;

	PostCategory(String label) {
		this.label = label;
	}
}