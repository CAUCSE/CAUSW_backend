package net.causw.app.main.domain.community.ceremony.enums;

import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

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
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.CEREMONY_INVALID_CONTEXT_VALUE);
		}

		for (CeremonyState ceremonyState : CeremonyState.values()) {
			if (ceremonyState.value.equalsIgnoreCase(state)) {
				return ceremonyState;
			}
		}

		throw new BadRequestException(
			ErrorCode.INVALID_PARAMETER,
			MessageUtil.CEREMONY_INVALID_CONTEXT_VALUE);
	}
}
