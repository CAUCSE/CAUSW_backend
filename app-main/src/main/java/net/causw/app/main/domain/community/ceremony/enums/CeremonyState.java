package net.causw.app.main.domain.community.ceremony.enums;

import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

import lombok.Getter;

@Getter
public enum CeremonyState {
	ACCEPT("accept"), // 승인
	REJECT("reject"), // 거절
	AWAIT("await"), // 대기
	CLOSE("close"); // 종료

	private final String value;

	CeremonyState(String value) {
		this.value = value;
	}

	public static CeremonyState fromString(String state) {
		if (state == null || state.isEmpty()) {
			throw GlobalErrorCode.BAD_REQUEST.toBaseException();
		}

		for (CeremonyState ceremonyState : CeremonyState.values()) {
			if (ceremonyState.value.equalsIgnoreCase(state)) {
				return ceremonyState;
			}
		}

		throw GlobalErrorCode.BAD_REQUEST.toBaseException();
	}
}
