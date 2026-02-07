package net.causw.app.main.domain.user.academic.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordLog;
import net.causw.app.main.domain.user.academic.repository.userAcademicRecord.UserAcademicRecordLogRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AcademicRecordLogCreator {

	private final UserAcademicRecordLogRepository logRepository;

	/**
	 * 학적 변경 신청에 대한 처리 로그를 생성한다.
	 *
	 * @param admin       처리한 관리자
	 * @param application 처리된 신청서 (상태가 이미 변경된 상태)
	 */
	public void createFromApplication(User admin, UserAcademicRecordApplication application) {
		UserAcademicRecordLog log = UserAcademicRecordLog.createWithApplication(admin, application);
		logRepository.save(log);
	}
}
