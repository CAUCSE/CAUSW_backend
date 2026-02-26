package net.causw.app.main.domain.user.academic.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordLog;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.academic.repository.userAcademicRecord.UserAcademicRecordLogRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.GraduationType;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class AcademicRecordLogCreatorTest {

	@Mock
	private UserAcademicRecordLogRepository logRepository;

	@InjectMocks
	private AcademicRecordLogCreator logCreator;

	@Test
	@DisplayName("졸업 로그 생성 시 요청 상태를 ACCEPT로 기록한다")
	void createGraduationLog_setsAcceptStatusAndRawNote() {
		User user = ObjectFixtures.getCertifiedUserWithId("user-1");
		when(logRepository.save(any(UserAcademicRecordLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UserAcademicRecordLog log = logCreator.createGraduationLog(user, 2026, GraduationType.AUGUST, "졸업 신청");

		assertThat(log.getTargetAcademicRecordStatus()).isEqualTo(AcademicStatus.GRADUATED);
		assertThat(log.getTargetAcademicRecordRequestStatus()).isEqualTo(AcademicRecordRequestStatus.ACCEPT);
		assertThat(log.getNote()).isEqualTo("졸업 신청");
		verify(logRepository).save(any(UserAcademicRecordLog.class));
	}

	@Test
	@DisplayName("졸업 로그 생성 시 note가 없으면 null로 저장한다")
	void createGraduationLog_withoutNote_setsNullNote() {
		User user = ObjectFixtures.getCertifiedUserWithId("user-2");
		when(logRepository.save(any(UserAcademicRecordLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UserAcademicRecordLog log = logCreator.createGraduationLog(user, 2026, GraduationType.FEBRUARY, "  ");

		assertThat(log.getTargetAcademicRecordRequestStatus()).isEqualTo(AcademicRecordRequestStatus.ACCEPT);
		assertThat(log.getNote()).isNull();
		verify(logRepository).save(any(UserAcademicRecordLog.class));
	}
}
