package net.causw.app.main.domain.community.ceremony.enums;

import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

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
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.CEREMONY_INVALID_CONTEXT_VALUE);
		}

		for (CeremonyType ceremonyType : CeremonyType.values()) {
			if (ceremonyType.value.equalsIgnoreCase(type)) {
				return ceremonyType;
			}
		}

		throw new BadRequestException(
			ErrorCode.INVALID_PARAMETER,
			MessageUtil.CEREMONY_INVALID_CONTEXT_VALUE);
	}
}
