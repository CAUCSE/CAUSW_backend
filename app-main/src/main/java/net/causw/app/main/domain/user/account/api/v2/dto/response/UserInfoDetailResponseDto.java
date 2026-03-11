package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.shared.dto.ProfileImageDto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserInfoDetailResponseDto(
	@Schema(description = "동문 수첩 프로필 id", example = "uuid 형식의 String 값입니다") String id,

	@Schema(description = "사용자 프로필 이미지") ProfileImageDto profileImage,

	@Schema(description = "사용자 이름", example = "홍길동") String name,

	@Schema(description = "사용자 학번", example = "18학번") String admissionYear,

	@Schema(description = "사용자 학적 상태", example = "졸업생") String academicStatus,

	@Schema(description = "사용자 직업", example = "풀스택 개발자") String job,

	@Schema(description = "동문 수첩 프로필 소개글", example = "동문 수첩 프로필 소개글입니다.") String description,

	@Schema(description = "사용자 전화번호", example = "010-1234-5678") String phoneNumber,

	@Schema(description = "연락처 공개 여부", example = "false") Boolean isPhoneNumberVisible,

	@Schema(description = "사용자 이메일", example = "abcde12345@cau.ac.kr") String email,

	@Schema(description = "사용자 SNS", example = "[\"https://www.example.com\"]") List<String> socialLinks,

	@Schema(description = "사용자 기술 스택", example = "[\"Stack0\", \"Stack1\"]") List<String> techStack,

	@Schema(description = "사용자 경력 사항") List<UserCareerDto> userCareer,

	@Schema(description = "사용자 대표 프로젝트") List<UserProjectDto> userProject,

	@Schema(description = "사용자 관심 기술", example = "[\"Tech0\", \"Tech1\"]") List<String> userInterestTech,

	@Schema(description = "사용자 관심 도메인", example = "[\"Domain0\", \"Domain1\"]") List<String> userInterestDomain) {
}