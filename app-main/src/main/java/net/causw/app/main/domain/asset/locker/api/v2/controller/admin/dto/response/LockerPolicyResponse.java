package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사물함 정책 조회 응답")
public record LockerPolicyResponse(

	@Schema(description = "만료일시") LocalDateTime expiredAt,

	@Schema(description = "신청 시작일시") LocalDateTime registerStartAt,

	@Schema(description = "신청 종료일시") LocalDateTime registerEndAt,

	@Schema(description = "연장 시작일시") LocalDateTime extendStartAt,

	@Schema(description = "연장 종료일시") LocalDateTime extendEndAt,

	@Schema(description = "다음 만료일시") LocalDateTime nextExpiredAt,

	@Schema(description = "사물함 신청/반납 가능 여부") Boolean isLockerAccessEnabled,

	@Schema(description = "사물함 연장 가능 여부") Boolean isLockerExtendEnabled) {
}
