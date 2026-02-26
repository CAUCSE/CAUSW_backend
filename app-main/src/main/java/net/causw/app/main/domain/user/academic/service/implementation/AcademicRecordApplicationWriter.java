package net.causw.app.main.domain.user.academic.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.academic.repository.userAcademicRecord.UserAcademicRecordApplicationRepository;
import net.causw.app.main.domain.user.academic.util.AcademicRecordApplicationValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AcademicRecordApplicationWriter {

	private final UserAcademicRecordApplicationRepository applicationRepository;
	private final UserRepository userRepository;

	/**
	 * 학적 변경 신청을 승인한다.
	 * - Application 상태를 AWAIT → ACCEPT로 변경
	 * - 대상 사용자의 학적 상태를 반영
	 */
	public void approve(UserAcademicRecordApplication application) {
		AcademicRecordApplicationValidator.validateAwaitStatus(application);

		User targetUser = application.getUser();

		// 대상 사용자의 학적 상태 변경
		targetUser.setAcademicStatus(application.getTargetAcademicStatus());
		userRepository.save(targetUser);

		// 신청서 상태 변경
		application.setAcademicRecordRequestStatus(AcademicRecordRequestStatus.ACCEPT);
		applicationRepository.save(application);
	}

	/**
	 * 학적 변경 신청을 반려한다.
	 * - Application 상태를 AWAIT → REJECT로 변경
	 * - 반려 사유 저장
	 */
	public void reject(UserAcademicRecordApplication application, String rejectReason) {
		AcademicRecordApplicationValidator.validateAwaitStatus(application);

		// 반려 사유 저장 및 상태 변경
		application.setRejectMessage(rejectReason);
		application.setAcademicRecordRequestStatus(AcademicRecordRequestStatus.REJECT);
		applicationRepository.save(application);
	}

	/**
	 * 재학 전환 신청서를 생성한다.
	 *
	 * @param user 신청 사용자
	 * @param currentCompletedSemester 신청 완료 학기 차수
	 * @param note 신청 메모
	 * @param attachImageList 신청 첨부 이미지 목록
	 * @return 저장된 신청서
	 */
	public UserAcademicRecordApplication createEnrollmentApplication(
		User user,
		Integer currentCompletedSemester,
		String note,
		List<UuidFile> attachImageList) {
		closeAwaitApplications(user);

		UserAcademicRecordApplication application = UserAcademicRecordApplication.createWithImage(
			user,
			AcademicRecordRequestStatus.AWAIT,
			AcademicStatus.ENROLLED,
			currentCompletedSemester,
			note,
			attachImageList);

		return applicationRepository.save(application);
	}

	private void closeAwaitApplications(User user) {
		List<UserAcademicRecordApplication> awaitApplications = applicationRepository
			.findByUserAndAcademicRecordRequestStatus(
				user,
				AcademicRecordRequestStatus.AWAIT);

		if (awaitApplications.isEmpty()) {
			return;
		}

		awaitApplications.forEach(application -> {
			application.setAcademicRecordRequestStatus(AcademicRecordRequestStatus.CLOSE);
			application.setRejectMessage(StaticValue.USER_CLOSED);
		});

		applicationRepository.saveAll(awaitApplications);
	}

}
