package net.causw.app.main.domain.asset.locker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LockerPeriodPhase {
	READY("아직 시작 전"),
	APPLY("신청 기간"),
	EXTEND("연장 기간"),
	CLOSED("종료");

	private final String description;
}
