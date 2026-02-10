package net.causw.app.main.domain.community.ceremony.enums;


import lombok.Getter;

@Getter
public enum CeremonyType {
	CELEBRATION("경사", "celebration"),
	CONDOLENCE("조사", "condolence");

	private final String label;
	private final String value;

	CeremonyType(String label, String value) {
		this.label = label;
		this.value = value;
	}
}
