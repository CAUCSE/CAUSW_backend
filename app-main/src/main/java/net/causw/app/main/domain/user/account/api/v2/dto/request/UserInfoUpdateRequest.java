package net.causw.app.main.domain.user.account.api.v2.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@Schema(description = "동문 수첩 프로필 수정 요청")
public record UserInfoUpdateRequest(
	@Size(max = 50, message = "직업은 최대 50자까지 입력 가능합니다.") @Schema(description = "사용자 직업 수정", example = "백엔드 개발자") String job,

	@Size(max = 200, message = "소개글은 최대 200자까지 입력 가능합니다.") @Schema(description = "동문 수첩 프로필 소개글 수정", example = "소개글 수정 내용입니다.") String description,

	@Schema(description = "전화 번호 공개 여부 수정", example = "true") boolean isPhoneNumberVisible,

	@Size(max = 10, message = "SNS는 최대 10개까지 등록할 수 있습니다.") @Schema(description = "사용자 SNS 수정", example = "[\"https://www.example.com\"]") List<String> socialLinks,

	@Schema(description = "사용자 기술 스택 수정", example = "[\"Stack0\", \"Stack1\"]") List<String> userTechStack,

	@Schema(description = "사용자 경력 사항 수정") List<@Valid UserCareerRequest> userCareer,

	@Schema(description = "사용자 대표 프로젝트 수정") List<@Valid UserProjectRequest> userProject,

	@Schema(description = "사용자 관심 기술 수정", example = "[\"Tech0\", \"Tech1\"]") List<String> userInterestTech,

	@Schema(description = "사용자 관심 도메인 수정", example = "[\"Domain0\", \"Domain1\"]") List<String> userInterestDomain) {
}
