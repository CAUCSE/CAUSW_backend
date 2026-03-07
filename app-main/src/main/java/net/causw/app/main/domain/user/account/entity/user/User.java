package net.causw.app.main.domain.user.account.entity.user;

import static net.causw.global.constant.StaticValue.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.BatchSize;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.campus.circle.entity.CircleMember;
import net.causw.app.main.domain.community.vote.entity.VoteRecord;
import net.causw.app.main.domain.notification.notification.entity.CeremonyNotificationSetting;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.api.v1.dto.GraduatedUserCommand;
import net.causw.app.main.domain.user.account.api.v1.dto.UserCreateRequestDto;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.GraduationType;
import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserRegisterDto;
import net.causw.app.main.domain.user.auth.service.dto.OAuthAttributes;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@Setter
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user")
public class User extends BaseEntity {
	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "phone_number", unique = true, nullable = true)
	private String phoneNumber;

	@Column(name = "password", nullable = true)
	private String password;

	@Column(name = "student_id", unique = true, nullable = true)
	private String studentId;

	@Column(name = "admission_year", nullable = true)
	private Integer admissionYear;

	// 새로 추가한 필드들
	@Column(name = "nickname", unique = true, nullable = true)
	private String nickname;

	// TODO: 기존값들 department로 마이그레이션 후 삭제
	@Column(name = "major", nullable = true)
	private String major;

	// TODO: null 임시 허용 제거
	@Column(name = "department", nullable = true)
	@Enumerated(EnumType.STRING)
	private Department department;

	@Column(name = "academic_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private AcademicStatus academicStatus;

	@Column(name = "current_completed_semester", nullable = true)
	private Integer currentCompletedSemester;

	/**
	 * @deprecated v1에서 관리자가 유저 학적에 대해 별도로 기록하던 메모용 필드.
	 * 사용 빈도가 낮아 v2에서는 해당 필드를 더 이상 사용하지 않는다.
	 */
	@Column(name = "academic_status_note", nullable = true)
	private String academicStatusNote;

	@Column(name = "graduation_year", nullable = true)
	private Integer graduationYear;

	@Enumerated(EnumType.STRING)
	@Column(name = "graduation_type", nullable = true)
	private GraduationType graduationType;

	@ElementCollection(fetch = FetchType.LAZY)
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	@BatchSize(size = 100)
	private Set<Role> roles;

	@Enumerated(EnumType.STRING)
	@Column(name = "profile_image_type", nullable = false)
	@Builder.Default
	private ProfileImageType profileImageType = ProfileImageType.MALE_1;

	@OneToOne(cascade = {CascadeType.REMOVE,
		CascadeType.PERSIST}, mappedBy = "user", fetch = FetchType.LAZY)
	private UserProfileImage userProfileImage;

	@Column(name = "state", nullable = false)
	@Enumerated(EnumType.STRING)
	private UserState state;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Embedded
	private TermAgreements agreements;

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
	private Locker locker;

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
	private CeremonyNotificationSetting ceremonyNotificationSetting;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<CircleMember> circleMemberList;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<VoteRecord> voteRecordList;

	@Column(name = "rejectionOrDropReason", nullable = true)
	private String rejectionOrDropReason;

	@Setter(AccessLevel.PUBLIC)
	@Column(name = "is_v2", nullable = false)
	@Builder.Default
	private Boolean isV2 = true;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "tb_user_fcm_token", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "fcm_token_value")
	private Set<String> fcmTokens = new HashSet<>();

	// 신고 관련 필드
	@Column(name = "report_count", nullable = false)
	@Builder.Default
	private Integer reportCount = 0;

	public void delete() {
		this.email = "deleted_" + this.getId();
		this.name = "탈퇴한 사용자";
		this.phoneNumber = null;
		this.studentId = null;
		this.nickname = null;
		this.major = null;
		this.profileImageType = ProfileImageType.GHOST;
		this.userProfileImage = null;
		this.graduationYear = null;
		this.graduationType = null;
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}

	public static User from(
		UserCreateRequestDto userCreateRequestDto,
		String encodedPassword) {
		return User.builder()
			.email(userCreateRequestDto.getEmail())
			.name(userCreateRequestDto.getName())
			.roles(Set.of(Role.NONE))
			.state(UserState.AWAIT)
			.password(encodedPassword)
			.studentId(userCreateRequestDto.getStudentId())
			.admissionYear(userCreateRequestDto.getAdmissionYear())
			.nickname(userCreateRequestDto.getNickname())
			.major(userCreateRequestDto.getMajor())
			.department(
				DepartmentResolver.resolveByAdmissionYearOrDepartment(
					userCreateRequestDto.getAdmissionYear(),
					userCreateRequestDto.getDepartment()))
			.academicStatus(AcademicStatus.UNDETERMINED)
			.phoneNumber(userCreateRequestDto.getPhoneNumber())
			.agreements(TermAgreements.createRequiredAgreements())
			.isV2(true)
			.build();
	}

	public static User from(UserRegisterDto dto, String encodedPassword) {
		return User.builder()
			.email(dto.email())
			.name(dto.name())
			.roles(Set.of(Role.NONE))
			.state(UserState.AWAIT)
			.password(encodedPassword)
			.nickname(dto.nickname())
			.academicStatus(AcademicStatus.UNDETERMINED)
			.phoneNumber(dto.phoneNumber())
			.agreements(TermAgreements.createRequiredAgreements())
			.isV2(true)
			.build();
	}

	public static User createGraduate(
		GraduatedUserCommand graduatedUserCommand,
		String encodedPassword) {
		return User.builder()
			.email(graduatedUserCommand.email())
			.name(graduatedUserCommand.name())
			.roles(Set.of(Role.COMMON))
			.state(UserState.ACTIVE)
			.password(encodedPassword)
			.studentId(graduatedUserCommand.studentId())
			.admissionYear(graduatedUserCommand.admissionYear())
			.graduationYear(graduatedUserCommand.graduationYear())
			.nickname(graduatedUserCommand.nickname())
			.department(graduatedUserCommand.department())
			.academicStatus(AcademicStatus.GRADUATED)
			.phoneNumber(graduatedUserCommand.phoneNumber())
			.agreements(TermAgreements.createRequiredAgreements())
			.isV2(true)
			.build();
	}

	public void updatePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public static User createSocialUser(OAuthAttributes attributes) {
		return User.builder()
			.email(attributes.email())
			.name(attributes.name())
			.roles(Set.of(Role.NONE))
			.state(UserState.GUEST)
			.academicStatus(AcademicStatus.UNDETERMINED)
			.isV2(true)
			.build();
	}

	public void updateProfile(String nickname, UserProfileImage userProfileImage, String phoneNumber) {
		this.nickname = nickname;
		this.userProfileImage = userProfileImage;
		if (phoneNumber != null && !NO_PHONE_NUMBER_MESSAGE.equals(phoneNumber)) {
			this.phoneNumber = phoneNumber;
		}
	}

	public void updateDetails(
		String email, String name, String phoneNumber, String encodedPassword,
		String studentId, Integer admissionYear, String nickname,
		String major, Department department) {
		this.email = email;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.password = encodedPassword;
		this.studentId = studentId;
		this.admissionYear = admissionYear;
		this.nickname = nickname;
		this.major = major;
		this.department = department;
		this.agreements = TermAgreements.createRequiredAgreements();
	}

	public void submitRegistration(String name, String nickname, String phoneNumber) {
		this.name = name;
		this.nickname = nickname;
		this.phoneNumber = phoneNumber;
		this.state = UserState.AWAIT;
		this.agreements = TermAgreements.createRequiredAgreements();
	}

	public void updateRejectionOrDropReason(String reason) {
		this.rejectionOrDropReason = reason;
	}

	// 재학 인증 신청시(UserAdmission 생성 시) 해당 유저가 신청 가능한 상태인지 확인
	public boolean canApplyAdmission() {
		return this.state == UserState.AWAIT || this.state == UserState.REJECT;
	}

	public void markAsAwait() {
		this.state = UserState.AWAIT;
		this.rejectionOrDropReason = null; // 거절 사유 초기화
	}

	// v2 재학인증 승인 시 신청서의 학적 정보를 사용자 계정에 반영하고 ACTIVE 상태로 전이한다.
	public void approveAdmission(UserAdmission admission) {
		this.studentId = admission.getRequestedStudentId();
		this.admissionYear = admission.getRequestedAdmissionYear();
		this.department = admission.getRequestedDepartment();
		this.academicStatus = admission.getRequestedAcademicStatus();
		this.graduationYear = admission.getRequestedGraduationYear();
		this.state = UserState.ACTIVE;
		this.rejectionOrDropReason = null;
		this.roles = Set.of(Role.COMMON);
	}

	// v2 재학인증 거절 시 사용자 상태를 REJECT로 전이하고 거절 사유를 기록한다.
	public void rejectAdmission(String rejectReason) {
		this.state = UserState.REJECT;
		this.rejectionOrDropReason = rejectReason;
	}

	public void markAsCertifiedGraduate(Integer graduationYear) {
		this.graduationYear = graduationYear;
		this.state = UserState.ACTIVE;
		this.roles = Set.of(Role.COMMON);
		this.academicStatus = AcademicStatus.GRADUATED;
		this.rejectionOrDropReason = null; // 거절 사유 초기화
	}

	public boolean removeFcmToken(String targetToken) {
		return this.fcmTokens.remove(targetToken);
	}

	public boolean isOnlySocialUser() {
		return this.password == null;
	}

	public String getProfileUrl() {
		if (this.profileImageType != ProfileImageType.CUSTOM) {
			return null;
		}
		if (this.userProfileImage == null || this.userProfileImage.getUuidFile() == null) {
			return null;
		}
		return this.userProfileImage.getUuidFile().getFileUrl();
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * 프로필 이미지를 기본 이미지(MALE_1, MALE_2, FEMALE_1, FEMALE_2)로 변경합니다.
	 * 기존 커스텀 이미지(UserProfileImage)는 null로 초기화됩니다.
	 */
	public void updateProfileImageToDefault(ProfileImageType defaultType) {
		if (defaultType == ProfileImageType.CUSTOM) {
			throw new IllegalArgumentException("기본 이미지 타입만 허용됩니다.");
		}
		this.profileImageType = defaultType;
		this.userProfileImage = null;
	}

	/**
	 * 프로필 이미지를 커스텀 이미지로 변경합니다.
	 * profileImageType을 CUSTOM으로 설정하고 UserProfileImage를 연결합니다.
	 */
	public void updateProfileImageToCustom(UserProfileImage newProfileImage) {
		this.profileImageType = ProfileImageType.CUSTOM;
		this.userProfileImage = newProfileImage;
	}

	// 신고 관련 메소드
	public void increaseReportCount() {
		this.reportCount++;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		User user = (User)o;
		return Objects.equals(getId(), user.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
