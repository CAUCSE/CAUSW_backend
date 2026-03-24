package net.causw.app.main.domain.user.account.service.dto.request;

import java.util.List;

public record UserInfoUpdateCommand(
	String job,
	String description,
	boolean isPhoneNumberVisible,
	List<String> socialLinks,
	List<String> userTechStack,
	List<UserCareerCommand> userCareer,
	List<UserProjectCommand> userProject,
	List<String> userInterestTech,
	List<String> userInterestDomain) {
}
