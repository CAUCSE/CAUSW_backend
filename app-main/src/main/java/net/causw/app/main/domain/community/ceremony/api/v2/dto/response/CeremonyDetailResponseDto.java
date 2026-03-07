package net.causw.app.main.domain.community.ceremony.api.v2.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

import io.swagger.v3.oas.annotations.media.Schema;

public record CeremonyDetailResponseDto(

	@Schema(description = "경조사 id", example = "uuid 형식의 String 값입니다.") String id,

	@Schema(description = "경조사 제목", example = "김철수(21학번) 딸 결혼식") String title,

	@Schema(description = "경조사 분류", example = "조사") String type,

	@Schema(description = "경조사 상세 분류", example = "결혼식") String category,

	@Schema(description = "경조사 시작 날짜", example = "2026-01-01") LocalDate startDate,

	@Schema(description = "경조사 종료 날짜", example = "2026-01-02") LocalDate endDate,

	@Schema(description = "경조사 시작 시간", example = "00:00") LocalTime startTime,

	@Schema(description = "경조사 종료 시간", example = "23:59") LocalTime endTime,

	@Schema(description = "경조사 신청자", example = "홍길동") String applicant,

	@Schema(description = "경조사 대상자", example = "김철수 딸") String subject,

	@Schema(description = "경조사 내용", example = "경조사 내용입니다.") String content,

	@Schema(description = "첨부 이미지 URL 리스트") List<String> attachedImageUrlList,

	@Schema(description = "경조사 주소", example = "서울특별시 동작구 흑석로 84") String address,

	@Schema(description = "경조사 우편 번호", example = "12345") String postalAddress,

	@Schema(description = "경조사 상세 주소", example = "중앙대학교 310관") String detailedAddress,

	@Schema(description = "경조사 문의 연락처", example = "010-1234-5678") String contact,

	@Schema(description = "경조사 관련 링크", example = "www.example.com/link") String link,

	@Schema(description = "모든 학번에게 알림 전송 여부 (권한 없으면 null)", example = "true") Boolean isSetAll,

	@Schema(description = "알림 대상 학번 (권한 없으면 null)", example = "[19, 21, 22]") List<String> targetAdmissionYears,

	@Schema(description = "신청한 경조사 상태", example = "AWAIT") CeremonyState state,

	@Schema(description = "경조사 신청 거부 사유", example = "경조사 신청 거부 사유입니다.") String note) {
}