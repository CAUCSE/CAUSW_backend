package net.causw.app.main.domain.community.ceremony.enums;

import lombok.Getter;

@Getter
public enum AlumniRelation {
	ALUMNI("동문 본인"),
	SPOUSE("배우자"),
	FATHER("부"),
	MOTHER("모"),
	FATHER_IN_LAW("장인"),
	MOTHER_IN_LAW("장모"),
	SON("아들"),
	DAUGHTER("딸");

	private final String label;

	AlumniRelation(String label) {
		this.label = label;
	}

}
