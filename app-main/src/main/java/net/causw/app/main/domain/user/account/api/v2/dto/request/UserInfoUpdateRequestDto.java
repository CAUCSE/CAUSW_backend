package net.causw.app.main.domain.user.account.api.v2.dto.request;

import java.util.List;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserCareerDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserProjectDto;

import jakarta.validation.constraints.Size;

public record UserInfoUpdateRequestDto(
	@Size(max = 50, message = "직업은 최대 50자까지 입력 가능합니다.") String job,

	@Size(max = 200, message = "소개글은 최대 200자까지 입력 가능합니다.") String description,

	boolean isPhoneNumberVisible,

	boolean isMessageVisible,

	@Size(max = 10, message = "SNS는 최대 10개까지 등록할 수 있습니다.") List<String> socialLinks,

	List<String> userTechStack,

	List<UserCareerDto> userCareer,

	List<UserProjectDto> userProject,

	List<String> userInterestTech,

	List<String> userInterestDomain) {
}
