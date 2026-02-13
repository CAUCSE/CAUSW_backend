package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.locker.entity.LockerStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 사물함 목록 조회 응답 아이템")
public record LockerListItemResponse(

	@Schema(description = "위치 설명", example = "2층") String location,

	@Schema(description = "사물함 번호", example = "1") Long lockerNumber,

	@Schema(description = "상태", example = "AVAILABLE") LockerStatus status,

	@Schema(description = "사용자 (이름(학번), 없으면 null)", example = "홍길동(20201234)") String user,

	@Schema(description = "만료일시", example = "2024-12-31T23:59:59") LocalDateTime expireDate) {
}
