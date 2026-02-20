package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사물함 연장 기간 설정 요청")
public record LockerPolicyExtendPeriodRequest(

	@Schema(description = "연장 시작일시 (ISO 8601)", example = "2026-07-01T00:00:00") @NotNull LocalDateTime extendStartAt,

	@Schema(description = "연장 종료일시 (ISO 8601)", example = "2026-07-15T23:59:59") @NotNull LocalDateTime extendEndAt,

	@Schema(description = "다음 만료일시 (ISO 8601)", example = "2027-02-28T23:59:59") @NotNull LocalDateTime nextExpiredAt) {

	@AssertTrue(message = "연장 종료일시는 연장 시작일시 이후여야 합니다.")
	private boolean isExtendEndAfterStart() {
		return extendEndAt.isAfter(extendStartAt);
	}

	@AssertTrue(message = "다음 만료일시는 연장 종료일시 이후여야 합니다.")
	private boolean isNextExpiredAfterExtendEnd() {
		return nextExpiredAt.isAfter(extendEndAt);
	}
}
