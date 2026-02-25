package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserInfoDetailResponseDto(
	@Schema(description = "동문 수첩 프로필 id", example = "uuid 형식의 String 값입니다") String id,

	@Schema(description = "사용자 프로필 이미지 URL") String profileImageUrl,

	@Schema(description = "사용자 이름", example = "홍길동") String name,

	@Schema(description = "사용자 학번", example = "18학번") String admissionYear,

	@Schema(description = "사용자 학적 상태", example = "졸업생") String academicStatus,

	@Schema(description = "사용자 직업", example = "풀스택 개발자") String job,

	@Schema(description = "동문 수첩 프로필 설명", example = "동문 수첩 프로필 설명입니다.") String description,

	@Schema(description = "사용자 전화번호", example = "010-1234-5678") String phoneNumber,

	@Schema(description = "전화번호 공개 여부", example = "false") Boolean isPhoneNumberVisible,

	@Schema(description = "메시지 공개 여부", example = "true") Boolean isMessageVisible,

	@Schema(description = "사용자 이메일", example = "abcde12345@cau.ac.kr") String email,

	@Schema(description = "사용자 SNS", example = "") List<String> socialLinks,

	@Schema(description = "사용자 기술 스택", example = "") Set<String> techStack,

	@Schema(description = "사용자 경력 사항", example = "") List<UserCareerDto> userCareer,

	@Schema(description = "사용자 대표 프로젝트", example = "") List<UserProjectDto> userProject,

	@Schema(description = "사용자 관심 기술", example = "") Set<String> userInterestTech,

	@Schema(description = "사용자 관심 도메인", example = "") Set<String> userInterestDomain) {
}