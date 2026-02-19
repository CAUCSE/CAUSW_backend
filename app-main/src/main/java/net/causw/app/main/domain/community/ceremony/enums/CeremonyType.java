package net.causw.app.main.domain.community.ceremony.enums;

import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

import lombok.Getter;

@Getter
public enum CeremonyType {
	CELEBRATION("경사"),
	CONDOLENCE("조사");

	private final String label;

	CeremonyType(String label) {
		this.label = label;
	}

	public static CeremonyType fromString(String type) {
		if (type == null || type.isEmpty()) {
			throw GlobalErrorCode.BAD_REQUEST.toBaseException();
		}
		for (CeremonyType ceremonyType : CeremonyType.values()) {
			if (ceremonyType.name().equalsIgnoreCase(type)) {
				return ceremonyType;
			}
		}
		throw GlobalErrorCode.BAD_REQUEST.toBaseException();
	}

	public static String parseTypeOrNull(String typeParam) {
		if (typeParam == null || typeParam.isEmpty() || typeParam.equalsIgnoreCase("ALL")) {
			return null;
		}
		return CeremonyType.fromString(typeParam).name();
	}
}
