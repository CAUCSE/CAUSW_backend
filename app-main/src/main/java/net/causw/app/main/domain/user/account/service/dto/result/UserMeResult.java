package net.causw.app.main.domain.user.account.service.dto.result;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.auth.enums.OnboardingStatus;
import net.causw.app.main.shared.dto.ProfileImageDto;

public record UserMeResult(
	String id,
	String name,
	String nickname,
	ProfileImageDto profileImage,
	Integer admissionYear,
	OnboardingStatus onboardingStatus,
	AcademicStatus academicStatus) {

	public static UserMeResult from(User user) {
		return new UserMeResult(
			user.getId(),
			user.getName(),
			user.getNickname(),
			ProfileImageDto.from(user),
			user.getAdmissionYear(),
			OnboardingStatus.resolve(user.isGuest(), user.isTermsAgreed(), user.isAcademicCertified()),
			user.getAcademicStatus());
	}
}
