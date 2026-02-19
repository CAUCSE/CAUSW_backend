package net.causw.app.main.domain.community.ceremony.enums;

import lombok.Getter;

@Getter
public enum CeremonyType {
	CELEBRATION("경사"),
	CONDOLENCE("조사");

	private final String label;

	CeremonyType(String label) {
		this.label = label;
	}

}
