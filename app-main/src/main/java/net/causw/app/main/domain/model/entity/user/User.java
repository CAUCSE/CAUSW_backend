package net.causw.app.main.domain.model.entity.user;

import static net.causw.global.constant.StaticValue.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.circle.CircleMember;
import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.notification.CeremonyNotificationSetting;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserProfileImage;
import net.causw.app.main.domain.model.entity.vote.VoteRecord;
import net.causw.app.main.domain.model.enums.user.Department;
import net.causw.app.main.domain.model.enums.user.GraduationType;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.resolver.DepartmentResolver;
import net.causw.app.main.dto.user.UserCreateRequestDto;
import net.causw.app.main.dto.user.CreateGraduatedUserCommand;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

	@Column(name = "phone_number", unique = true, nullable = false)
	private String phoneNumber;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "student_id", unique = true, nullable = true)
	private String studentId;

	@Column(name = "admission_year", nullable = false)
	private Integer admissionYear;

	// 새로 추가한 필드들
	@Column(name = "nickname", unique = true, nullable = false)
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

	@Column(name = "academic_status_note", nullable = true)
	private String academicStatusNote;

	@Column(name = "graduation_year", nullable = true)
	private Integer graduationYear;

	@Enumerated(EnumType.STRING)
	@Column(name = "graduation_type", nullable = true)
	private GraduationType graduationType;

	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Set<Role> roles;

	@OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, mappedBy = "user")
	private UserProfileImage userProfileImage;

	@Column(name = "state", nullable = false)
	@Enumerated(EnumType.STRING)
	private UserState state;

	@OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
	private Locker locker;

	@OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
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
	@CollectionTable(
		name = "tb_user_fcm_token",
		joinColumns = @JoinColumn(name = "user_id")
	)
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
		this.userProfileImage = null;
		this.graduationYear = null;
		this.graduationType = null;
		this.state = UserState.DELETED;
	}

	public static User from(
		UserCreateRequestDto userCreateRequestDto,
		String encodedPassword
	) {
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
				DepartmentResolver.resolveByAdmissionYearOrRequest(
					userCreateRequestDto.getAdmissionYear(),
					userCreateRequestDto.getDepartment()
			))
			.academicStatus(AcademicStatus.UNDETERMINED)
			.phoneNumber(userCreateRequestDto.getPhoneNumber())
			.isV2(true)
			.build();
	}

	public static User createGraduatedUser(
		CreateGraduatedUserCommand createGraduatedUserCommand,
		String encodedPassword
	) {
		return User.builder()
			.email(createGraduatedUserCommand.email())
			.name(createGraduatedUserCommand.name())
			.roles(Set.of(Role.COMMON))
			.state(UserState.ACTIVE)
			.password(encodedPassword)
			.studentId(createGraduatedUserCommand.studentId())
			.admissionYear(createGraduatedUserCommand.admissionYear())
			.graduationYear(createGraduatedUserCommand.graduationYear())
			.nickname(createGraduatedUserCommand.nickname())
			.major(createGraduatedUserCommand.major())

			.academicStatus(AcademicStatus.GRADUATED)
			.phoneNumber(createGraduatedUserCommand.phoneNumber())
			.isV2(true)
			.build();
	}

	public void update(String nickname, UserProfileImage userProfileImage, String phoneNumber) {
		this.nickname = nickname;
		this.userProfileImage = userProfileImage;
		if (phoneNumber != null && !NO_PHONE_NUMBER_MESSAGE.equals(phoneNumber)) {
			this.phoneNumber = phoneNumber;
		}
	}

	public void updateRejectionOrDropReason(String reason) {
		this.rejectionOrDropReason = reason;
	}

	public void updateInfo(UserCreateRequestDto userCreateRequestDto, String encodedPassword) {
		this.email = userCreateRequestDto.getEmail();
		this.name = userCreateRequestDto.getName();
		this.nickname = userCreateRequestDto.getNickname();
		this.phoneNumber = userCreateRequestDto.getPhoneNumber();
		this.studentId = userCreateRequestDto.getStudentId();
		this.admissionYear = userCreateRequestDto.getAdmissionYear();
		this.major = userCreateRequestDto.getMajor();
		this.password = encodedPassword;
		this.state = UserState.AWAIT;
		this.rejectionOrDropReason = null; // 거절 사유 초기화
	}

	public void removeFcmToken(String targetToken) {
		this.fcmTokens.remove(targetToken);
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
