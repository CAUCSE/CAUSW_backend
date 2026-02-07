package net.causw.app.main.domain.user.academic.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.repository.userAcademicRecord.UserAcademicRecordApplicationRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.shared.exception.errorcode.AcademicRecordApplicationErrorCode;

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
		validateAwaitStatus(application);

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
		validateAwaitStatus(application);

		// 반려 사유 저장 및 상태 변경
		application.setRejectMessage(rejectReason);
		application.setAcademicRecordRequestStatus(AcademicRecordRequestStatus.REJECT);
		applicationRepository.save(application);
	}

	private void validateAwaitStatus(UserAcademicRecordApplication application) {
		if (application.getAcademicRecordRequestStatus() != AcademicRecordRequestStatus.AWAIT) {
			throw AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_AWAITING.toBaseException();
		}
	}
}
