package net.causw.app.main.domain.user.account.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record AdmissionResult(
	String id,
	String userName,
	String userEmail,
	Department requestedDepartment,
	Integer requestedAdmissionYear,
	String requestedStudentId,
	AcademicStatus requestedAcademicStatus,
	Integer requestedGraduationYear,
	String description,
	List<String> attachImageUrls,
	UserState userState,
	LocalDateTime createdAt,
	LocalDateTime updatedAt) {

	public static AdmissionResult from(UserAdmission admission) {
		List<String> imageUrls = admission.getUserAdmissionAttachImageList().stream()
			.map(image -> image.getUuidFile().getFileUrl())
			.toList();

		return new AdmissionResult(
			admission.getId(),
			admission.getUser().getName(),
			admission.getUser().getEmail(),
			admission.getRequestedDepartment(),
			admission.getRequestedAdmissionYear(),
			admission.getRequestedStudentId(),
			admission.getRequestedAcademicStatus(),
			admission.getRequestedGraduationYear(),
			admission.getDescription(),
			imageUrls,
			admission.getUser().getState(),
			admission.getCreatedAt(),
			admission.getUpdatedAt());
	}
}
