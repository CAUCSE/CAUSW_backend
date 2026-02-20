package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.locker.enums.LockerLogAction;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 사물함 로그 목록 조회 응답 아이템")
public record LockerLogListItemResponse(

	@Schema(description = "로그 ID", example = "log-uuid-1234") String id,

	@Schema(description = "사물함 번호", example = "1") Long lockerNumber,

	@Schema(description = "사물함 위치명", example = "SECOND") String lockerLocationName,

	@Schema(description = "사용자 이메일", example = "user@example.com") String userEmail,

	@Schema(description = "사용자 이름", example = "홍길동") String userName,

	@Schema(description = "액션", example = "REGISTER") LockerLogAction action,

	@Schema(description = "메시지") String message,

	@Schema(description = "생성일시", example = "2024-12-31T23:59:59") LocalDateTime createdAt) {
}
