package net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.request;

import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.enums.LockerLogAction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "관리자 사물함 로그 목록 조회 요청")
public record LockerLogListRequest(

	@Schema(description = "사용자 검색 (이름, 이메일)", example = "홍길동") String userKeyword,

	@Schema(description = "액션 필터 (ENABLE, DISABLE, REGISTER, RETURN, EXTEND)", example = "REGISTER") LockerLogAction action,

	@Schema(description = "위치 필터 (SECOND, THIRD, FOURTH)", example = "SECOND") LockerName lockerLocationName,

	@Schema(description = "사물함 번호 필터", example = "1") Long lockerNumber,

	@Schema(description = "페이지 번호 (0부터 시작)", example = "0", minimum = "0") @Min(0) Integer page,

	@Schema(description = "페이지 크기", example = "10", minimum = "1", maximum = "100") @Min(1) @Max(100) Integer size) {
}
