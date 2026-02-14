package net.causw.app.main.domain.user.account.entity.user;

import java.util.ArrayList;
import java.util.List;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserAdmissionAttachImage;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_admission")
public class UserAdmission extends BaseEntity {
	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "userAdmission")
	@Builder.Default
	private List<UserAdmissionAttachImage> userAdmissionAttachImageList = new ArrayList<>();

	@Column(name = "description", nullable = true)
	private String description;

	// ── v2 확장 필드: 신청 시 기입한 학적 정보 (v1 데이터는 null) ──

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

	/**
	 * v1 팩토리 메서드 (기존 호환)
	 */
	public static UserAdmission ofV1(User requestUser, List<UuidFile> userAdmissionAttachImageUuidFileList,
									 String description) {
		UserAdmission userAdmission = UserAdmission.builder()
			.user(requestUser)
			.description(description)
			.build();

		List<UserAdmissionAttachImage> userAdmissionAttachImageList = userAdmissionAttachImageUuidFileList.stream()
			.map(uuidFile -> UserAdmissionAttachImage.of(userAdmission, uuidFile))
			.toList();

		userAdmission.setUserAdmissionAttachImageList(userAdmissionAttachImageList);

		return userAdmission;
	}

	/**
	 * v2 팩토리 메서드 — 신청 시 기입한 학적 정보를 포함하여 생성합니다.
	 * 학적 필드는 코드 레벨에서 not-null을 보장합니다.
	 */
	public static UserAdmission of(
		User requestUser,
		List<UuidFile> userAdmissionAttachImageUuidFileList,
		String description,
		AcademicStatus requestedAcademicStatus,
		String requestedStudentId,
		Integer requestedAdmissionYear,
		Department requestedDepartment) {
		UserAdmission userAdmission = UserAdmission.builder()
			.user(requestUser)
			.description(description)
			.requestedAcademicStatus(requestedAcademicStatus)
			.requestedStudentId(requestedStudentId)
			.requestedAdmissionYear(requestedAdmissionYear)
			.requestedDepartment(requestedDepartment)
			.build();

		List<UserAdmissionAttachImage> userAdmissionAttachImageList = userAdmissionAttachImageUuidFileList.stream()
			.map(uuidFile -> UserAdmissionAttachImage.of(userAdmission, uuidFile))
			.toList();

		userAdmission.setUserAdmissionAttachImageList(userAdmissionAttachImageList);

		return userAdmission;
	}

	private void setUserAdmissionAttachImageList(List<UserAdmissionAttachImage> userAdmissionAttachImageList) {
		this.userAdmissionAttachImageList = userAdmissionAttachImageList;
	}
}
