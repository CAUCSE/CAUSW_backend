package net.causw.app.main.domain.admin.audit.event;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCreateCommand;
import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminAuditLogEventPublisher {

	private static final String TARGET_TYPE_LOCKER = "LOCKER";
	private static final String TARGET_TYPE_USER = "USER";

	private static final String META_LOCKER_ID = "사물함ID";
	private static final String META_LOCKER_NUMBER = "사물함번호";
	private static final String META_LOCKER_LOCATION_NAME = "사물함위치";
	private static final String META_EXPIRE_DATE = "만료일";
	private static final String META_RELEASED_USER_ID = "회수자ID";
	private static final String META_EXPIRED_AT = "연장만료일";

	private static final String META_ADMISSION_ID = "재학인증신청ID";
	private static final String META_REQUESTED_ACADEMIC_STATUS = "요청학적상태";
	private static final String META_REQUESTED_STUDENT_ID = "요청학번";
	private static final String META_REQUESTED_ADMISSION_YEAR = "요청입학년도";
	private static final String META_REQUESTED_DEPARTMENT = "요청학과";
	private static final String META_REQUESTED_GRADUATION_YEAR = "요청졸업년도";
	private static final String META_REJECT_REASON = "반려사유";

	private static final String META_APPLICATION_ID = "학적변경신청ID";
	private static final String META_BEFORE_ACADEMIC_STATUS = "변경전학적상태";
	private static final String META_TARGET_ACADEMIC_STATUS = "변경후학적상태";
	private static final String META_NOTE = "요청메모";

	private final ApplicationEventPublisher eventPublisher;

	public void publishLockerAssign(Locker locker, User admin, User assignee, LocalDateTime expiredAt) {
		publish(lockerCommand(
			"ASSIGN",
			"사물함 배정",
			locker,
			admin,
			Optional.of(assignee),
			"%s 사물함을 %s에게 배정했습니다.".formatted(lockerName(locker), assignee.getName()),
			lockerMetadataWithExpiredAt(locker, expiredAt)));
	}

	public void publishLockerExtend(Locker locker, User admin, User assignee, LocalDateTime expiredAt) {
		publish(lockerCommand(
			"EXTEND",
			"사물함 연장",
			locker,
			admin,
			Optional.of(assignee),
			"%s 사물함 사용 기간을 연장했습니다.".formatted(lockerName(locker)),
			lockerMetadataWithExpiredAt(locker, expiredAt)));
	}

	public void publishLockerRelease(Locker locker, User admin, User assignee) {
		publish(lockerCommand(
			"RELEASE",
			"사물함 회수",
			locker,
			admin,
			Optional.of(assignee),
			"%s 사물함을 회수했습니다.".formatted(lockerName(locker)),
			lockerMetadata(locker, Map.of())));
	}

	public void publishLockerEnable(Locker locker, User admin) {
		publish(lockerCommand(
			"ENABLE",
			"사물함 활성화",
			locker,
			admin,
			Optional.empty(),
			"%s 사물함을 활성화했습니다.".formatted(lockerName(locker)),
			lockerMetadata(locker, Map.of())));
	}

	public void publishLockerDisable(Locker locker, User admin, Optional<User> releasedUser) {
		publish(lockerCommand(
			"DISABLE",
			"사물함 비활성화",
			locker,
			admin,
			releasedUser,
			"%s 사물함을 비활성화했습니다.".formatted(lockerName(locker)),
			lockerMetadataWithReleasedUser(locker, releasedUser)));
	}

	public void publishLockerReleaseExpired(Locker locker, User admin, Optional<User> assignee) {
		publish(lockerCommand(
			"RELEASE_EXPIRED",
			"만료 사물함 일괄 회수",
			locker,
			admin,
			assignee,
			"%s 만료 사물함을 회수했습니다.".formatted(lockerName(locker)),
			lockerMetadata(locker, Map.of())));
	}

	public void publishAdmissionAccept(UserAdmission admission, User adminUser) {
		User targetUser = admission.getUser();
		publish(userCommand(
			"ADMISSION_ACCEPT",
			"재학인증 승인",
			adminUser,
			targetUser,
			"%s의 재학인증 신청을 승인했습니다.".formatted(targetUser.getName()),
			admissionMetadata(admission, null)));
	}

	public void publishAdmissionReject(UserAdmission admission, User adminUser, String rejectReason) {
		User targetUser = admission.getUser();
		publish(userCommand(
			"ADMISSION_REJECT",
			"재학인증 거절",
			adminUser,
			targetUser,
			"%s의 재학인증 신청을 거절했습니다.".formatted(targetUser.getName()),
			admissionMetadata(admission, rejectReason)));
	}

	public void publishAcademicRecordAccept(User adminUser, UserAcademicRecordApplication application) {
		User targetUser = application.getUser();
		publish(userCommand(
			"ACADEMIC_RECORD_ACCEPT",
			"학적변경 승인",
			adminUser,
			targetUser,
			"%s의 학적변경 신청을 승인했습니다.".formatted(targetUser.getName()),
			academicRecordMetadata(application, null)));
	}

	public void publishAcademicRecordReject(
		User adminUser,
		UserAcademicRecordApplication application,
		String rejectReason) {
		User targetUser = application.getUser();
		publish(userCommand(
			"ACADEMIC_RECORD_REJECT",
			"학적변경 반려",
			adminUser,
			targetUser,
			"%s의 학적변경 신청을 반려했습니다.".formatted(targetUser.getName()),
			academicRecordMetadata(application, rejectReason)));
	}

	public void publishUserAction(
		User adminUser,
		User targetUser,
		String actionType,
		String actionDescription,
		String summary,
		Map<String, Object> metadata) {
		publish(userActionCommand(
			actionType,
			actionDescription,
			adminUser,
			targetUser,
			summary,
			metadata));
	}

	private void publish(AdminAuditLogCreateCommand command) {
		eventPublisher.publishEvent(new AdminAuditLogEvent(command));
	}

	private AdminAuditLogCreateCommand lockerCommand(
		String actionType,
		String actionDescription,
		Locker locker,
		User admin,
		Optional<User> targetUser,
		String summary,
		Map<String, Object> metadata) {
		return new AdminAuditLogCreateCommand(
			AdminAuditLogCategory.LOCKER,
			actionType,
			actionDescription,
			admin.getId(),
			admin.getEmail(),
			admin.getName(),
			admin.getStudentId(),
			TARGET_TYPE_LOCKER,
			locker.getId(),
			targetUser.map(User::getEmail).orElse(null),
			targetUser.map(User::getName).orElse(null),
			targetUser.map(User::getStudentId).orElse(null),
			summary,
			metadata);
	}

	private AdminAuditLogCreateCommand userCommand(
		String actionType,
		String actionDescription,
		User adminUser,
		User targetUser,
		String summary,
		Map<String, Object> metadata) {
		return new AdminAuditLogCreateCommand(
			AdminAuditLogCategory.ACADEMIC,
			actionType,
			actionDescription,
			adminUser.getId(),
			adminUser.getEmail(),
			adminUser.getName(),
			adminUser.getStudentId(),
			TARGET_TYPE_USER,
			targetUser.getId(),
			targetUser.getEmail(),
			targetUser.getName(),
			targetUser.getStudentId(),
			summary,
			metadata);
	}

	private AdminAuditLogCreateCommand userActionCommand(
		String actionType,
		String actionDescription,
		User adminUser,
		User targetUser,
		String summary,
		Map<String, Object> metadata) {
		return new AdminAuditLogCreateCommand(
			AdminAuditLogCategory.USER,
			actionType,
			actionDescription,
			adminUser.getId(),
			adminUser.getEmail(),
			adminUser.getName(),
			adminUser.getStudentId(),
			TARGET_TYPE_USER,
			targetUser.getId(),
			targetUser.getEmail(),
			targetUser.getName(),
			targetUser.getStudentId(),
			summary,
			metadata);
	}

	private Map<String, Object> lockerMetadata(Locker locker, Map<String, Object> additionalMetadata) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put(META_LOCKER_ID, locker.getId());
		metadata.put(META_LOCKER_NUMBER, locker.getLockerNumber());
		metadata.put(META_LOCKER_LOCATION_NAME, locker.getLocation().getName());
		metadata.put(META_EXPIRE_DATE, locker.getExpireDate());
		additionalMetadata.forEach((key, value) -> {
			if (value != null) {
				metadata.put(key, value);
			}
		});
		return metadata;
	}

	private Map<String, Object> lockerMetadataWithReleasedUser(Locker locker, Optional<User> releasedUser) {
		Map<String, Object> metadata = lockerMetadata(locker, Map.of());
		releasedUser.map(User::getId).ifPresent(userId -> metadata.put(META_RELEASED_USER_ID, userId));
		return metadata;
	}

	private Map<String, Object> lockerMetadataWithExpiredAt(Locker locker, LocalDateTime expiredAt) {
		Map<String, Object> metadata = lockerMetadata(locker, Map.of());
		if (expiredAt != null) {
			metadata.put(META_EXPIRED_AT, expiredAt);
		}
		return metadata;
	}

	private Map<String, Object> admissionMetadata(UserAdmission admission, String rejectReason) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		putIfNotNull(metadata, META_ADMISSION_ID, admission.getId());
		putIfNotNull(metadata, META_REQUESTED_ACADEMIC_STATUS, admission.getRequestedAcademicStatus());
		putIfNotNull(metadata, META_REQUESTED_STUDENT_ID, admission.getRequestedStudentId());
		putIfNotNull(metadata, META_REQUESTED_ADMISSION_YEAR, admission.getRequestedAdmissionYear());
		putIfNotNull(metadata, META_REQUESTED_DEPARTMENT, admission.getRequestedDepartment());
		putIfNotNull(metadata, META_REQUESTED_GRADUATION_YEAR, admission.getRequestedGraduationYear());
		putIfNotNull(metadata, META_REJECT_REASON, rejectReason);
		return metadata;
	}

	private Map<String, Object> academicRecordMetadata(
		UserAcademicRecordApplication application,
		String rejectReason) {
		User targetUser = application.getUser();
		Map<String, Object> metadata = new LinkedHashMap<>();
		putIfNotNull(metadata, META_APPLICATION_ID, application.getId());
		putIfNotNull(metadata, META_BEFORE_ACADEMIC_STATUS, targetUser.getAcademicStatus());
		putIfNotNull(metadata, META_TARGET_ACADEMIC_STATUS, application.getTargetAcademicStatus());
		putIfNotNull(metadata, META_NOTE, application.getNote());
		putIfNotNull(metadata, META_REJECT_REASON, rejectReason);
		return metadata;
	}

	private void putIfNotNull(Map<String, Object> metadata, String key, Object value) {
		if (value != null) {
			metadata.put(key, value);
		}
	}

	private String lockerName(Locker locker) {
		return "%s-%d".formatted(locker.getLocation().getName(), locker.getLockerNumber());
	}
}
