package net.causw.app.main.domain.user.account.service.dto.result;

import java.util.List;

public record UserInfoDetailResult(
	String id,
	String profileImageUrl,
	String name,
	String admissionYear,
	String academicStatus,
	String job,
	String description,
	String phoneNumber,
	Boolean isPhoneNumberVisible,
	String email,
	List<String> socialLinks,
	List<String> techStack,
	List<UserCareerResult> userCareer,
	List<UserProjectResult> userProject,
	List<String> userInterestTech,
	List<String> userInterestDomain) {
}