package net.causw.app.main.domain.community.ceremony.api.v2.dto.request;

import java.time.LocalDate;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 경조사 목록 조회 요청")
public record CeremonyAdminListRequest(

	@Schema(description = "검색 시작일 (경조사 시작일 기준)", example = "2026-01-01") LocalDate fromDate,

	@Schema(description = "검색 종료일 (경조사 시작일 기준)", example = "2026-12-31") LocalDate toDate,

	@Schema(description = "경조사 상태 (AWAIT, ACCEPT, REJECT, CLOSE)", example = "AWAIT") CeremonyState state) {
}
