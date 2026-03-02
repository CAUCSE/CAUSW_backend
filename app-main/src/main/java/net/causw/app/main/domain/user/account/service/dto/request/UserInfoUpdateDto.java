package net.causw.app.main.domain.user.account.service.dto.request;

import java.util.List;

import jakarta.validation.constraints.Size;

public record UserInfoUpdateDto(
	@Size(max = 50, message = "직업은 최대 50자까지 입력 가능합니다.") String job,

	@Size(max = 200, message = "소개글은 최대 200자까지 입력 가능합니다.") String description,

	boolean isPhoneNumberVisible,

	@Size(max = 10, message = "SNS는 최대 10개까지 등록할 수 있습니다.") List<String> socialLinks,

	List<String> userTechStack,

	List<UserCareerCommand> userCareer,

	List<UserProjectCommand> userProject,

	List<String> userInterestTech,

	List<String> userInterestDomain) {
}
