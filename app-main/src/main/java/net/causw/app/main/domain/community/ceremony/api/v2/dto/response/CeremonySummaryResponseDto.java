package net.causw.app.main.domain.community.ceremony.api.v2.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

import io.swagger.v3.oas.annotations.media.Schema;

public record CeremonySummaryResponseDto(

	@Schema(description = "경조사 id", example = "uuid 형식의 String 값입니다.") String id,

	@Schema(description = "경조사 제목", example = "김철수(21학번) 딸 결혼식") String title,

	@Schema(description = "경조사 분류", example = "조사") String type,

	@Schema(description = "경조사 상세 분류", example = "결혼식 (직접 입력일 경우 '직접 입력')") String category,

	@Schema(description = "경조사 시작 날짜", example = "2026-01-01") LocalDate startDate,

	@Schema(description = "경조사 종료 날짜", example = "2026-01-02") LocalDate endDate,

	@Schema(description = "경조사 시작 시간", example = "00:00") LocalTime startTime,

	@Schema(description = "경조사 종료 시간", example = "23:59") LocalTime endTime,

	@Schema(description = "신청한 경조사 상태", example = "AWAIT") CeremonyState state) {
}