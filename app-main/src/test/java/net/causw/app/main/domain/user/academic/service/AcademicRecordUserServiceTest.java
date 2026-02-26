package net.causw.app.main.domain.user.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.user.academic.api.v2.dto.request.EnrollmentApplicationRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.request.GraduationApplicationRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicStatusResponse;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.EnrollmentDetailsResponse;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.GraduationDetailsResponse;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordLog;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.academic.event.AcademicStatusChangeEvent;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationWriter;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordLogCreator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.GraduationType;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class AcademicRecordUserServiceTest {

	@Mock
	private UserReader userReader;

	@Mock
	private UserWriter userWriter;

	@Mock
	private UuidFileService uuidFileService;

	@Mock
	private AcademicRecordApplicationWriter applicationWriter;

	@Mock
	private AcademicRecordLogCreator logCreator;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private AcademicRecordUserService academicRecordUserService;

	@Test
	@DisplayName("재학 사용자가 졸업 상태로 변경 요청하면 로그만 생성하고 학적 상태를 즉시 변경한다")
	void updateStatusToGraduated_success() {
		User user = ObjectFixtures.getCertifiedUserWithId("user-1");
		GraduationApplicationRequest request = new GraduationApplicationRequest(2026, GraduationType.AUGUST, "졸업 신청");

		UserAcademicRecordLog log = UserAcademicRecordLog.createWithGraduation(
			user,
			user,
			AcademicStatus.GRADUATED,
			2026,
			GraduationType.AUGUST,
			"졸업 신청");
		ReflectionTestUtils.setField(log, "id", "log-1");
		ReflectionTestUtils.setField(log, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));

		when(userReader.findUserById(user.getId())).thenReturn(user);
		when(logCreator.createGraduationLog(user, 2026, GraduationType.AUGUST, "졸업 신청")).thenReturn(log);

		AcademicStatusResponse<GraduationDetailsResponse> response = academicRecordUserService.updateStatusToGraduated(
			user,
			request);

		assertThat(response.requestedStatus()).isEqualTo(AcademicStatus.GRADUATED);
		assertThat(response.updatedStatus()).isEqualTo(AcademicStatus.GRADUATED);
		assertThat(response.recordDetails().logId()).isEqualTo("log-1");
		assertThat(user.getGraduationYear()).isEqualTo(2026);
		assertThat(user.getGraduationType()).isEqualTo(GraduationType.AUGUST);

		verify(userWriter).save(user);
		verify(eventPublisher).publishEvent(any(AcademicStatusChangeEvent.class));
		verify(logCreator).createGraduationLog(user, 2026, GraduationType.AUGUST, "졸업 신청");
		verify(applicationWriter, never()).createEnrollmentApplication(any(), any(), any(), any());
	}

	@Test
	@DisplayName("졸업 사용자가 재학 변경 요청하면 신청서를 생성한다")
	void updateStatusToEnrolled_success() {
		User user = ObjectFixtures.getCertifiedUserWithId("user-2");
		user.setAcademicStatus(AcademicStatus.GRADUATED);

		EnrollmentApplicationRequest request = new EnrollmentApplicationRequest(5, "복학 신청");
		MockMultipartFile image = new MockMultipartFile("imageFileList", "proof.png", "image/png",
			new byte[] {1, 2, 3});

		UuidFile uuidFile = UuidFile.of(
			"uuid-1",
			"file-key",
			"https://example.com/proof.png",
			"proof",
			"png",
			FilePath.USER_ACADEMIC_RECORD_APPLICATION);

		UserAcademicRecordApplication application = UserAcademicRecordApplication.createWithImage(
			user,
			AcademicRecordRequestStatus.AWAIT,
			AcademicStatus.ENROLLED,
			5,
			"복학 신청",
			List.of(uuidFile));
		ReflectionTestUtils.setField(application, "id", "application-1");
		ReflectionTestUtils.setField(application, "createdAt", LocalDateTime.of(2026, 3, 1, 10, 0));

		when(userReader.findUserById(user.getId())).thenReturn(user);
		when(uuidFileService.saveFileList(List.of(image), FilePath.USER_ACADEMIC_RECORD_APPLICATION))
			.thenReturn(List.of(uuidFile));
		when(applicationWriter.createEnrollmentApplication(user, 5, "복학 신청", List.of(uuidFile)))
			.thenReturn(application);

		AcademicStatusResponse<EnrollmentDetailsResponse> response = academicRecordUserService.updateStatusToEnrolled(
			user,
			request,
			List.of(image));

		assertThat(response.requestedStatus()).isEqualTo(AcademicStatus.ENROLLED);
		assertThat(response.updatedStatus()).isEqualTo(AcademicStatus.GRADUATED);
		assertThat(response.recordDetails().applicationId()).isEqualTo("application-1");
		assertThat(response.recordDetails().requestStatus()).isEqualTo(AcademicRecordRequestStatus.AWAIT);
		assertThat(response.recordDetails().requestedAt()).isEqualTo(LocalDateTime.of(2026, 3, 1, 10, 0));

		verify(uuidFileService).saveFileList(List.of(image), FilePath.USER_ACADEMIC_RECORD_APPLICATION);
		verify(applicationWriter).createEnrollmentApplication(user, 5, "복학 신청", List.of(uuidFile));
		verify(logCreator, never()).createFromApplication(any(), any());
		verify(userWriter, never()).save(any(User.class));
		verify(eventPublisher, never()).publishEvent(any(Object.class));
	}

	@Test
	@DisplayName("졸업 상태가 아닌 사용자가 재학 변경 요청하면 예외가 발생한다")
	void updateStatusToEnrolled_invalidTransition() {
		User user = ObjectFixtures.getCertifiedUserWithId("user-3");
		EnrollmentApplicationRequest request = new EnrollmentApplicationRequest(3, "복학 신청");
		MockMultipartFile image = new MockMultipartFile("imageFileList", "proof.png", "image/png", new byte[] {1});

		when(userReader.findUserById(user.getId())).thenReturn(user);

		assertThatThrownBy(() -> academicRecordUserService.updateStatusToEnrolled(user, request, List.of(image)))
			.isInstanceOf(BaseRunTimeV2Exception.class);

		verifyNoInteractions(uuidFileService, applicationWriter, logCreator, userWriter);
	}

	@Test
	@DisplayName("재학 변경 요청 시 빈 이미지 파트만 전달되면 예외가 발생한다")
	void updateStatusToEnrolled_emptyImagePart_throwsException() {
		User user = ObjectFixtures.getCertifiedUserWithId("user-4");
		user.setAcademicStatus(AcademicStatus.GRADUATED);
		EnrollmentApplicationRequest request = new EnrollmentApplicationRequest(4, "복학 신청");
		MockMultipartFile emptyImage = new MockMultipartFile("imageFileList", "", "application/octet-stream",
			new byte[0]);

		when(userReader.findUserById(user.getId())).thenReturn(user);

		assertThatThrownBy(() -> academicRecordUserService.updateStatusToEnrolled(user, request, List.of(emptyImage)))
			.isInstanceOf(BaseRunTimeV2Exception.class);

		verifyNoInteractions(uuidFileService, applicationWriter, logCreator, userWriter, eventPublisher);
	}
}
