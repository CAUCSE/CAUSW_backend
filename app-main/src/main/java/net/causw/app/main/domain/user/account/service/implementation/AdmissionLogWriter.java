package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserAdmissionAttachImage;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.entity.user.UserAdmissionLog;
import net.causw.app.main.domain.user.account.enums.user.UserAdmissionLogAction;
import net.causw.app.main.domain.user.account.repository.user.UserAdmissionLogRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class AdmissionLogWriter {

	private final UserAdmissionLogRepository userAdmissionLogRepository;

	/**
	 * v2 재학인증 승인 로그를 생성합니다.
	 */
	public UserAdmissionLog createAcceptLog(UserAdmission admission, User adminUser) {
		UserAdmissionLog log = UserAdmissionLog.of(
			admission.getUser().getEmail(),
			admission.getUser().getName(),
			adminUser.getEmail(),
			adminUser.getName(),
			UserAdmissionLogAction.ACCEPT,
			extractUuidFiles(admission),
			admission.getDescription(),
			null,
			admission.getRequestedAcademicStatus(),
			admission.getRequestedStudentId(),
			admission.getRequestedAdmissionYear(),
			admission.getRequestedDepartment(),
			admission.getRequestedGraduationYear());

		return userAdmissionLogRepository.save(log);
	}

	/**
	 * v2 재학인증 거절 로그를 생성합니다.
	 */
	public UserAdmissionLog createRejectLog(UserAdmission admission, User adminUser, String rejectReason) {
		UserAdmissionLog log = UserAdmissionLog.of(
			admission.getUser().getEmail(),
			admission.getUser().getName(),
			adminUser.getEmail(),
			adminUser.getName(),
			UserAdmissionLogAction.REJECT,
			extractUuidFiles(admission),
			admission.getDescription(),
			rejectReason,
			admission.getRequestedAcademicStatus(),
			admission.getRequestedStudentId(),
			admission.getRequestedAdmissionYear(),
			admission.getRequestedDepartment(),
			admission.getRequestedGraduationYear());

		return userAdmissionLogRepository.save(log);
	}

	private List<UuidFile> extractUuidFiles(UserAdmission admission) {
		return admission.getUserAdmissionAttachImageList().stream()
			.map(UserAdmissionAttachImage::getUuidFile)
			.toList();
	}
}
