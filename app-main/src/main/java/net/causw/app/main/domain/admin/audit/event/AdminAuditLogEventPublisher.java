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

	private Map<String, Object> lockerMetadata(Locker locker, Map<String, Object> additionalMetadata) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("lockerId", locker.getId());
		metadata.put("lockerNumber", locker.getLockerNumber());
		metadata.put("lockerLocationName", locker.getLocation().getName());
		metadata.put("expireDate", locker.getExpireDate());
		additionalMetadata.forEach((key, value) -> {
			if (value != null) {
				metadata.put(key, value);
			}
		});
		return metadata;
	}

	private Map<String, Object> lockerMetadataWithReleasedUser(Locker locker, Optional<User> releasedUser) {
		Map<String, Object> metadata = lockerMetadata(locker, Map.of());
		releasedUser.map(User::getId).ifPresent(userId -> metadata.put("releasedUserId", userId));
		return metadata;
	}

	private Map<String, Object> lockerMetadataWithExpiredAt(Locker locker, LocalDateTime expiredAt) {
		Map<String, Object> metadata = lockerMetadata(locker, Map.of());
		if (expiredAt != null) {
			metadata.put("expiredAt", expiredAt);
		}
		return metadata;
	}

	private Map<String, Object> admissionMetadata(UserAdmission admission, String rejectReason) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		putIfNotNull(metadata, "admissionId", admission.getId());
		putIfNotNull(metadata, "requestedAcademicStatus", admission.getRequestedAcademicStatus());
		putIfNotNull(metadata, "requestedStudentId", admission.getRequestedStudentId());
		putIfNotNull(metadata, "requestedAdmissionYear", admission.getRequestedAdmissionYear());
		putIfNotNull(metadata, "requestedDepartment", admission.getRequestedDepartment());
		putIfNotNull(metadata, "requestedGraduationYear", admission.getRequestedGraduationYear());
		putIfNotNull(metadata, "rejectReason", rejectReason);
		return metadata;
	}

	private Map<String, Object> academicRecordMetadata(
		UserAcademicRecordApplication application,
		String rejectReason) {
		User targetUser = application.getUser();
		Map<String, Object> metadata = new LinkedHashMap<>();
		putIfNotNull(metadata, "applicationId", application.getId());
		putIfNotNull(metadata, "beforeAcademicStatus", targetUser.getAcademicStatus());
		putIfNotNull(metadata, "targetAcademicStatus", application.getTargetAcademicStatus());
		putIfNotNull(metadata, "note", application.getNote());
		putIfNotNull(metadata, "rejectReason", rejectReason);
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
