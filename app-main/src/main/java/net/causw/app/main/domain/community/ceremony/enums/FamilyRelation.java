package net.causw.app.main.domain.community.ceremony.enums;

import lombok.Getter;

@Getter
public enum FamilyRelation {
	SPOUSE("배우자"),
	FATHER("부"),
	MOTHER("모"),
	FATHER_IN_LAW("장인"),
	MOTHER_IN_LAW("장모"),
	SON("아들"),
	DAUGHTER("딸"),
	BROTHERS("형제"),
	SISTERS("자매"),
	SIBLINGS("남매"),
	GRANDFATHER("조부"),
	GRANDMOTHER("조모");

	private final String label;

	FamilyRelation(String label) {
		this.label = label;
	}

}
