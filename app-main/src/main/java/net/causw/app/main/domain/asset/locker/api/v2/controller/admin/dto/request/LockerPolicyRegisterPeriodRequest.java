package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사물함 신청 기간 설정 요청")
public record LockerPolicyRegisterPeriodRequest(

	@Schema(description = "신청 시작일시 (ISO 8601)", example = "2026-03-01T00:00:00") @NotNull LocalDateTime registerStartAt,

	@Schema(description = "신청 종료일시 (ISO 8601)", example = "2026-03-15T23:59:59") @NotNull LocalDateTime registerEndAt,

	@Schema(description = "만료일시 (ISO 8601)", example = "2026-08-31T23:59:59") @NotNull LocalDateTime expiredAt) {

	@AssertTrue(message = "신청 종료일시는 신청 시작일시 이후여야 합니다.")
	private boolean isRegisterEndAfterStart() {
		return registerEndAt.isAfter(registerStartAt);
	}

	@AssertTrue(message = "만료일시는 신청 종료일시 이후여야 합니다.")
	private boolean isExpiredAfterRegisterEnd() {
		return expiredAt.isAfter(registerEndAt);
	}
}
