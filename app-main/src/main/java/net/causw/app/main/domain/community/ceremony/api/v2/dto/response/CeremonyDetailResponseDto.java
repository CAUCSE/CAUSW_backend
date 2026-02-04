package net.causw.app.main.domain.community.ceremony.api.v2.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CeremonyDetailResponseDto {

	@Schema(description = "경조사 id", example = "uuid 형식의 String 값입니다.")
	private String id;

	@Schema(description = "경조사 제목", example = "김철수(21학번) 딸 결혼식")
	private String title;

	@Schema(description = "경조사 분류", example = "CELEBRATION")
	private CeremonyType type;

	@Schema(description = "경조사 상세 분류", example = "결혼식")
	private String category;

	@Schema(description = "경조사 시작 날짜", example = "2026-01-01")
	private LocalDate startDate;

	@Schema(description = "경조사 종료 날짜", example = "2026-01-02")
	private LocalDate endDate;

	@Schema(description = "경조사 시작 시간", example = "00:00")
	private LocalTime startTime;

	@Schema(description = "경조사 종료 시간", example = "23:59")
	private LocalTime endTime;

	@Schema(description = "경조사 신청자", example = "홍길동")
	private String applicant;

	@Schema(description = "경조사 대상자", example = "김철수 딸")
	private String subject;

	@Schema(description = "경조사 내용", example = "경조사 내용입니다.")
	private String content;

	@Schema(description = "첨부 이미지 URL 리스트")
	private List<String> attachedImageUrlList;

	@Schema(description = "경조사 주소", example = "서울특별시 동작구 흑석로 84")
	private String address;

	@Schema(description = "경조사 우편 번호", example = "12345")
	private String postalAddress;

	@Schema(description = "경조사 상세 주소", example = "중앙대학교 310관")
	private String detailedAddress;

	@Schema(description = "경조사 문의 연락처", example = "010-1234-5678")
	private String contact;

	@Schema(description = "경조사 관련 링크", example = "www.example.com/link")
	private String link;

	@Schema(description = "모든 학번에게 알림 전송 여부 (권한 없으면 null)", example = "true")
	private Boolean isSetAll;

	@Schema(description = "알림 대상 학번 (권한 없으면 null)", example = "[19, 21, 22]")
	private List<String> targetAdmissionYears;

	@Schema(description = "신청한 경조사 상태", example = "AWAIT")
	private CeremonyState state;

	@Schema(description = "경조사 신청 거부 사유", example = "경조사 신청 거부 사유입니다.")
	private String note;
}
