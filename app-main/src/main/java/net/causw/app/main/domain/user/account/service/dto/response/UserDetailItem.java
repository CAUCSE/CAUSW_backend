package net.causw.app.main.domain.user.account.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.GraduationType;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record UserDetailItem(
	String id,
	String email,
	String name,
	String studentId,
	Integer admissionYear,
	List<String> roles,
	String profileImageUrl,
	UserState state,
	String nickname,
	String major,
	Department department,
	AcademicStatus academicStatus,
	Integer graduationYear,
	GraduationType graduationType,
	String phoneNumber,
	String rejectionOrDropReason,
	LocalDateTime createdAt,
	LocalDateTime updatedAt) {

	public static UserDetailItem from(User user) {
		return new UserDetailItem(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getStudentId(),
			user.getAdmissionYear(),
			user.getRoles().stream().map(Enum::name).toList(),
			extractProfileImageUrl(user),
			user.getState(),
			user.getNickname(),
			user.getMajor(),
			user.getDepartment(),
			user.getAcademicStatus(),
			user.getGraduationYear(),
			user.getGraduationType(),
			user.getPhoneNumber(),
			user.getRejectionOrDropReason(),
			user.getCreatedAt(),
			user.getUpdatedAt());
	}

	private static String extractProfileImageUrl(User user) {
		return user.getProfileUrl();
	}
}
