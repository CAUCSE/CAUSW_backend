package net.causw.app.main.domain.user.account.service.dto.result;

import java.util.List;

import net.causw.app.main.shared.dto.ProfileImageDto;

public record UserInfoDetailResult(
	String id,
	ProfileImageDto profileImage,
	String name,
	String admissionYear,
	String academicStatus,
	String job,
	String description,
	String phoneNumber,
	Boolean isPhoneNumberVisible,
	String email,
	List<String> socialLinks,
	List<String> userTechStack,
	List<UserCareerResult> userCareer,
	List<UserProjectResult> userProject,
	List<String> userInterestTech,
	List<String> userInterestDomain) {
}