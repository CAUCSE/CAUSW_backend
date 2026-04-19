package net.causw.app.main.domain.user.account.service.dto.result;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.auth.enums.OnboardingStatus;
import net.causw.app.main.shared.dto.ProfileImageDto;

public record UserMeAccountResult(
	String id,
	String email,
	String name,
	String nickname,
	ProfileImageDto profileImage,
	Integer admissionYear,
	Integer graduationYear,
	String job,
	OnboardingStatus onboardingStatus,
	AcademicStatus academicStatus,
	String phoneNumber,
	String studentId,
	Department department) {

	public static UserMeAccountResult from(User user, UserInfo userInfo, boolean hasAllRequiredLatestTerms) {
		return new UserMeAccountResult(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getNickname(),
			ProfileImageDto.from(user),
			user.getAdmissionYear(),
			user.getGraduationYear(),
			userInfo != null ? userInfo.getJob() : null,
			OnboardingStatus.resolve(user.isGuest(), hasAllRequiredLatestTerms, user.isAcademicCertified()),
			user.getAcademicStatus(),
			user.getPhoneNumber(),
			user.getStudentId(),
			user.getDepartment());
	}
}
