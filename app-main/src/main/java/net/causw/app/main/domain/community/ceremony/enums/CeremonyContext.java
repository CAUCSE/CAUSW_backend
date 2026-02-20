package net.causw.app.main.domain.community.ceremony.enums;

import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

import lombok.Getter;

@Getter
public enum CeremonyContext {
	GENERAL("general", "전체 알림 페이지에서 접근"),
	MY("my", "나의 경조사 페이지에서 접근"),
	ADMIN("admin", "경조사 관리 페이지에서 접근");

	private final String value;
	private final String description;

	CeremonyContext(String value, String description) {
		this.value = value;
		this.description = description;
	}

	// String을 enum으로 변환
	public static CeremonyContext fromString(String context) {
		if (context == null || context.isEmpty()) {
			throw CeremonyErrorCode.INVALID_CEREMONY_CONTEXT.toBaseException();
		}

		for (CeremonyContext ceremonyContext : CeremonyContext.values()) {
			if (ceremonyContext.value.equalsIgnoreCase(context)) {
				return ceremonyContext;
			}
		}

		throw CeremonyErrorCode.INVALID_CEREMONY_CONTEXT.toBaseException();
	}
}
