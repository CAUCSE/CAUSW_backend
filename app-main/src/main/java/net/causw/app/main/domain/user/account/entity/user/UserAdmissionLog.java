package net.causw.app.main.domain.user.account.entity.user;

import java.util.ArrayList;
import java.util.List;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserAdmissionLogAttachImage;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserAdmissionLogAction;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_admission_log")
public class UserAdmissionLog extends BaseEntity {
	@Column(name = "user_email", nullable = false)
	private String userEmail;

	@Column(name = "user_name", nullable = false)
	private String userName;

	@Column(name = "admin_user_email", nullable = false)
	private String adminUserEmail;

	@Column(name = "admin_user_name", nullable = false)
	private String adminUserName;

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "userAdmissionLog")
	@Builder.Default
	private List<UserAdmissionLogAttachImage> userAdmissionLogAttachImageList = new ArrayList<>();

	@Column(name = "description")
	private String description;

	@Column(name = "action", nullable = false)
	@Enumerated(EnumType.STRING)
	private UserAdmissionLogAction action;

	@Column(name = "rejectReason", nullable = true)
	private String rejectReason;

	// ── v2 확장 필드: 신청 시 기입한 학적 정보 (v1 로그는 null) ──

	@Enumerated(EnumType.STRING)
	@Column(name = "requested_academic_status", nullable = true)
	private AcademicStatus requestedAcademicStatus;

	@Column(name = "requested_student_id", nullable = true)
	private String requestedStudentId;

	@Column(name = "requested_admission_year", nullable = true)
	private Integer requestedAdmissionYear;

	@Enumerated(EnumType.STRING)
	@Column(name = "requested_department", nullable = true)
	private Department requestedDepartment;

	@Column(name = "requested_graduation_year", nullable = true)
	private Integer requestedGraduationYear;

	/**
	 * v1 팩토리 메서드 (기존 호환)
	 */
	public static UserAdmissionLog ofV1(
		String userEmail,
		String userName,
		String adminUserEmail,
		String adminUserName,
		UserAdmissionLogAction action,
		List<UuidFile> userAdmissionLogAttachImageUuidFileList,
		String description,
		String rejectReason) {
		UserAdmissionLog userAdmissionLog = UserAdmissionLog.builder()
			.userEmail(userEmail)
			.userName(userName)
			.adminUserEmail(adminUserEmail)
			.adminUserName(adminUserName)
			.action(action)
			.description(description)
			.rejectReason(rejectReason)
			.build();

		List<UserAdmissionLogAttachImage> userAdmissionLogAttachImageList = userAdmissionLogAttachImageUuidFileList
			.stream()
			.map(uuidFile -> UserAdmissionLogAttachImage.of(userAdmissionLog, uuidFile))
			.toList();

		userAdmissionLog.setUserAdmissionLogAttachImageList(userAdmissionLogAttachImageList);

		return userAdmissionLog;
	}

	/**
	 * v2 팩토리 메서드 — 신청 시 기입한 학적 정보를 포함하여 생성합니다.
	 */
	public static UserAdmissionLog of(
		String userEmail,
		String userName,
		String adminUserEmail,
		String adminUserName,
		UserAdmissionLogAction action,
		List<UuidFile> userAdmissionLogAttachImageUuidFileList,
		String description,
		String rejectReason,
		AcademicStatus requestedAcademicStatus,
		String requestedStudentId,
		Integer requestedAdmissionYear,
		Department requestedDepartment,
		Integer requestedGraduationYear) {
		UserAdmissionLog userAdmissionLog = UserAdmissionLog.builder()
			.userEmail(userEmail)
			.userName(userName)
			.adminUserEmail(adminUserEmail)
			.adminUserName(adminUserName)
			.action(action)
			.description(description)
			.rejectReason(rejectReason)
			.requestedAcademicStatus(requestedAcademicStatus)
			.requestedStudentId(requestedStudentId)
			.requestedAdmissionYear(requestedAdmissionYear)
			.requestedDepartment(requestedDepartment)
			.requestedGraduationYear(requestedGraduationYear)
			.build();

		List<UserAdmissionLogAttachImage> userAdmissionLogAttachImageList = userAdmissionLogAttachImageUuidFileList
			.stream()
			.map(uuidFile -> UserAdmissionLogAttachImage.of(userAdmissionLog, uuidFile))
			.toList();

		userAdmissionLog.setUserAdmissionLogAttachImageList(userAdmissionLogAttachImageList);

		return userAdmissionLog;
	}

	private void setUserAdmissionLogAttachImageList(List<UserAdmissionLogAttachImage> userAdmissionLogAttachImageList) {
		this.userAdmissionLogAttachImageList = userAdmissionLogAttachImageList;
	}
}
