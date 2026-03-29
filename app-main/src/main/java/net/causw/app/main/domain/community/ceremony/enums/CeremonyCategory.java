package net.causw.app.main.domain.community.ceremony.enums;

import lombok.Getter;

@Getter
public enum CeremonyCategory {
	MARRIAGE("결혼식"),
	FIRST_BIRTHDAY("돌잔치"),
	OPENING("개업"),
	BIRTHDAY("생신잔치"),

	FUNERAL("장례식"),
	ACCIDENT("사고"),
	ILLNESS("투병"),

	GRADUATION("졸업식"), // TODO: v2에서는 GRADUATION 제거 후 ETC로 대체

	ETC("직접 입력");

	private final String label;

	CeremonyCategory(String label) {
		this.label = label;
	}

}
