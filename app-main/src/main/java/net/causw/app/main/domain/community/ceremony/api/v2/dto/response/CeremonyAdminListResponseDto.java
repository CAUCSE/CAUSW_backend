package net.causw.app.main.domain.community.ceremony.api.v2.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 경조사 목록 조회 응답")
public record CeremonyAdminListResponseDto(

	@Schema(description = "경조사 id", example = "uuid 형식의 String 값입니다.") String id,

	@Schema(description = "신청자 이름", example = "홍길동") String applicantName,

	@Schema(description = "신청자 학번", example = "20210001") String applicantStudentId,

	@Schema(description = "경조사 상태", example = "AWAIT") CeremonyState state,

	@Schema(description = "경조사 시작일", example = "2026-01-01") LocalDate startDate,

	@Schema(description = "신청일", example = "2026-01-01T00:00:00") LocalDateTime createdAt,

	@Schema(description = "경조사 상세 분류", example = "결혼식") String category) {
}
