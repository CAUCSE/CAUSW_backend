package net.causw.app.main.domain.community.ceremony.enums;

import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

import lombok.Getter;

@Getter
public enum CeremonyState {
	ACCEPT, // 승인
	REJECT, // 거절
	AWAIT, // 대기
	CLOSE; // 종료

	public static CeremonyState fromString(String state) {
		if (state == null || state.isEmpty()) {
			throw GlobalErrorCode.BAD_REQUEST.toBaseException();
		}

		for (CeremonyState ceremonyState : CeremonyState.values()) {
			if (ceremonyState.name().equalsIgnoreCase(state)) {
				return ceremonyState;
			}
		}

		throw GlobalErrorCode.BAD_REQUEST.toBaseException();
	}
}
