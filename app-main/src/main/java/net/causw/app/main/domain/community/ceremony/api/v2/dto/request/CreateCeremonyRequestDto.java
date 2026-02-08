package net.causw.app.main.domain.community.ceremony.api.v2.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.enums.RelationType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class CreateCeremonyRequestDto {

	@Schema(description = "경조사 분류", requiredMode = Schema.RequiredMode.REQUIRED, example = "CELEBRATION")
	@NotNull(message = "분류는 필수 입력 값입니다.")
	private CeremonyType ceremonyType;

	@Schema(description = "경조사 상세 분류", requiredMode = Schema.RequiredMode.REQUIRED, example = "결혼식")
	@NotNull(message = "상세 분류는 필수 입력 값입니다.")
	private String ceremonyCategory;

	@Schema(description = "경조사 시작 날짜", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-01-01")
	@NotNull(message = "시작 날짜는 필수 입력 값입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@Schema(description = "경조사 끝 날짜", example = "2026-01-02")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@Schema(description = "경조사 시작 시간", example = "00:00")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime startTime;

	@Schema(description = "경조사 종료 시간", example = "23:59")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime endTime;

	@Schema(description = "경조사 신청자-대상자 관계 (ME/FAMILY/ALUMNI)", requiredMode = Schema.RequiredMode.REQUIRED, example = "FAMILY")
	@NotNull(message = "관계는 필수 입력 값입니다.")
	private RelationType relationType;

	@Schema(description = "경조사 신청자-대상자 가족 관계", example = "아들")
	private String familyRelation;

	@Schema(description = "경조사 신청자 동문-대상자 관계 (동문 본인인 경우 '동문')", example = "아들")
	private String alumniRelation;

	@Schema(description = "경조사 신청자의 동문 이름", example = "김철수")
	private String alumniName;

	@Schema(description = "경조사 신청자의 동문 학번 (입학년도)", example = "2021")
	private String alumniAdmissionYear;

	@Schema(description = "경조사 내용", example = "경조사 내용입니다.")
	private String content;

	@Schema(description = "경조사 주소", example = "서울특별시 동작구 흑석로 84")
	private String address;

	@Schema(description = "경조사 우편 번호", example = "12345")
	private String postalAddress;

	@Schema(description = "경조사 상세 주소", example = "중앙대학교 310관")
	private String detailedAddress;

	@Schema(description = "경조사 문의 연락처", example = "010-1234-5678")
	@Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
	private String contact;

	@Schema(description = "경조사 관련 링크", example = "www.example.com/link")
	private String link;

	@Schema(description = "모든 학번에게 알림 전송 여부", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
	@NotNull(message = "전체 알림 전송 여부는 필수 입력 값입니다.")
	private Boolean isSetAll;

	@Schema(description = "알림 대상 학번", requiredMode = Schema.RequiredMode.REQUIRED, example = "[19, 21, 22]")
	private List<String> targetAdmissionYears;

	// 학번 검증
	@AssertTrue(message = "동문 학번은 4자리로 입력해야 합니다. (ex. 1972, 2005, 2021)")
	private boolean isValidAdmissionYear() {
		if (alumniAdmissionYear != null) {
			return alumniAdmissionYear.matches("^(19|20)[0-9]{2}$");
		}
		return true;
	}
}
