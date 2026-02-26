package net.causw.app.main.domain.user.academic.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordLog;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.academic.repository.userAcademicRecord.UserAcademicRecordLogRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.GraduationType;

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
	public UserAcademicRecordLog createFromApplication(User admin, UserAcademicRecordApplication application) {
		UserAcademicRecordLog log = UserAcademicRecordLog.createWithApplication(admin, application);
		return logRepository.save(log);
	}

	/**
	 * 졸업 상태 변경 로그를 생성한다.
	 *
	 * @param requester 요청 사용자
	 * @param graduationYear 졸업년도
	 * @param graduationType 졸업 유형
	 * @param note 사용자 메모
	 * @return 저장된 로그
	 */
	public UserAcademicRecordLog createGraduationLog(
		User requester,
		Integer graduationYear,
		GraduationType graduationType,
		String note) {
		String normalizedNote = normalizeNote(note);

		UserAcademicRecordLog log = UserAcademicRecordLog.createWithGraduation(
			requester,
			requester,
			AcademicStatus.GRADUATED,
			graduationYear,
			graduationType,
			normalizedNote,
			AcademicRecordRequestStatus.ACCEPT);

		return logRepository.save(log);
	}

	private String normalizeNote(String note) {
		if (note == null || note.isBlank()) {
			return null;
		}

		return note;
	}
}
