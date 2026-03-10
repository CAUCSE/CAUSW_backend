package net.causw.app.main.domain.community.ceremony.api.v2.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.enums.RelationType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "경조사 신청 요청")
public record CreateCeremonyRequest(
	@NotNull(message = "분류는 필수 입력 값입니다.") @Schema(description = "경조사 분류", requiredMode = Schema.RequiredMode.REQUIRED, example = "CELEBRATION") CeremonyType ceremonyType,

	@NotNull(message = "상세 분류는 필수 입력 값입니다.") @Schema(description = "경조사 상세 분류 (enum)", example = "MARRIAGE") CeremonyCategory ceremonyCategory,

	@Size(max = 30, message = "상세 분륜는 최대 30자까지 입력 가능합니다.") @Schema(description = "경조사 상세 분류 (직접 입력)", example = "졸업식") String ceremonyCustomCategory,

	@NotNull(message = "시작 날짜는 필수 입력 값입니다.") @DateTimeFormat(pattern = "yyyy-MM-dd") @Schema(description = "경조사 시작 날짜", example = "2026-01-01") LocalDate startDate,

	@DateTimeFormat(pattern = "yyyy-MM-dd") @Schema(description = "경조사 끝 날짜", example = "2026-01-02") LocalDate endDate,

	@DateTimeFormat(pattern = "HH:mm") @Schema(description = "경조사 시작 시간", example = "00:00") LocalTime startTime,

	@DateTimeFormat(pattern = "HH:mm") @Schema(description = "경조사 종료 시간", example = "23:59") LocalTime endTime,

	@NotNull(message = "관계는 필수 입력 값입니다.") @Schema(description = "경조사 신청자-대상자 관계 (ME/FAMILY/ALUMNI)", example = "FAMILY") RelationType relationType,

	@Schema(description = "경조사 신청자-대상자 가족 관계", example = "아들") String familyRelation,

	@Schema(description = "경조사 신청자 동문-대상자 관계 (동문 본인인 경우 '본인')", example = "아들") String alumniRelation,

	@Size(max = 20, message = "동문 이름은 최대 20자까지 입력 가능합니다.") @Schema(description = "경조사 신청자의 동문 이름", example = "김철수") String alumniName,

	@Pattern(regexp = "^(19|20)[0-9]{2}$", message = "동문 학번은 4자리로 입력해야 합니다.") @Schema(description = "경조사 신청자의 동문 학번 (입학년도)", example = "2021") String alumniAdmissionYear,

	@Size(max = 250, message = "경조사 내용은 최대 250자까지 입력 가능합니다.") @Schema(description = "경조사 내용", example = "경조사 내용입니다.") String content,

	@Schema(description = "경조사 주소", example = "서울특별시 동작구 흑석로 84") String address,

	@Schema(description = "경조사 우편 번호", example = "12345") String postalAddress,

	@Schema(description = "경조사 상세 주소", example = "중앙대학교 310관") String detailedAddress,

	@Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.") @Schema(description = "경조사 문의 연락처", example = "010-1234-5678") String contact,

	@URL(protocol = "https", message = "관련 링크는 https://로 시작해야 합니다.") @Schema(description = "경조사 관련 링크", example = "www.example.com/link") String link,

	@NotNull(message = "전체 알림 전송 여부는 필수 입력 값입니다.") @Schema(description = "모든 학번에게 알림 전송 여부", example = "true") Boolean isSetAll,

	@Schema(description = "알림 대상 학번", example = "[19, 21, 22]") List<@Pattern(regexp = "^[0-9]{2}$", message = "알림 대상 학번 형식이 올바르지 않습니다.") String> targetAdmissionYears) {
}
