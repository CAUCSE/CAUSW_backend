package net.causw.app.main.domain.community.ceremony.enums;

import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

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

	public static CeremonyType fromString(String type) {
		if (type == null || type.isEmpty()) {
			throw GlobalErrorCode.BAD_REQUEST.toBaseException();
		}

		for (CeremonyType ceremonyType : CeremonyType.values()) {
			if (ceremonyType.value.equalsIgnoreCase(type)) {
				return ceremonyType;
			}
		}

		throw GlobalErrorCode.BAD_REQUEST.toBaseException();
	}
}
