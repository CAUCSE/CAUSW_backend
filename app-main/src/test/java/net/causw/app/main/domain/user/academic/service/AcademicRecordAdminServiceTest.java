package net.causw.app.main.domain.user.academic.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.academic.service.dto.request.AcademicRecordApplicationListCondition;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicRecordApplicationDetailResult;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicRecordApplicationSummaryResult;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationReader;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationWriter;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordLogCreator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AcademicRecordApplicationErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class AcademicRecordAdminServiceTest {

	@Mock
	private AcademicRecordApplicationReader applicationReader;

	@Mock
	private AcademicRecordApplicationWriter applicationWriter;

	@Mock
	private AcademicRecordLogCreator logCreator;

	@InjectMocks
	private AcademicRecordAdminService academicRecordAdminService;

	@Nested
	@DisplayName("학적 변경 신청 조회")
	class GetApplication {

		@Test
		@DisplayName("학적 변경 신청 목록을 조회한다")
		void getApplications_success() {
			// given
			AcademicRecordApplicationListCondition condition = new AcademicRecordApplicationListCondition(
				AcademicRecordRequestStatus.AWAIT,
				null,
				null,
				0,
				10);

			User user = ObjectFixtures.getCertifiedUser();

			UserAcademicRecordApplication application = UserAcademicRecordApplication.create(
				user,
				AcademicRecordRequestStatus.AWAIT,
				AcademicStatus.ENROLLED,
				null,
				"복학 신청합니다");

			Page<UserAcademicRecordApplication> mockPage = new PageImpl<>(
				List.of(application),
				PageRequest.of(0, 10),
				1);

			when(applicationReader.findApplications(condition))
				.thenReturn(mockPage);

			// when
			Page<AcademicRecordApplicationSummaryResult> result = academicRecordAdminService.getApplications(condition);

			// then
			verify(applicationReader).findApplications(condition);
			assert result.getTotalElements() == 1;
		}

		@Test
		@DisplayName("학적 변경 신청 상세를 조회한다")
		void getApplicationDetail_success() {
			// given
			String applicationId = "application-id";
			User user = ObjectFixtures.getCertifiedUser();

			UserAcademicRecordApplication application = UserAcademicRecordApplication.create(
				user,
				AcademicRecordRequestStatus.AWAIT,
				AcademicStatus.ENROLLED,
				null,
				"복학 신청합니다");

			when(applicationReader.findById(applicationId))
				.thenReturn(application);

			// when
			AcademicRecordApplicationDetailResult result = academicRecordAdminService
				.getApplicationDetail(applicationId);

			// then
			verify(applicationReader).findById(applicationId);
			assert result.userName().equals(user.getName());
		}

		@Test
		@DisplayName("존재하지 않는 신청 ID로 상세 조회 시 예외가 발생한다")
		void getApplicationDetail_notFound() {
			// given
			String applicationId = "non-existent-id";

			when(applicationReader.findById(applicationId))
				.thenThrow(
					AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_FOUND
						.toBaseException());

			// when & then
			assertThatThrownBy(() -> academicRecordAdminService.getApplicationDetail(applicationId))
				.isInstanceOf(BaseRunTimeV2Exception.class);
		}
	}

	@Nested
	@DisplayName("학적 변경 신청 승인")
	class Approve {

		@Test
		@DisplayName("학적 변경 신청을 승인한다")
		void approve_success() {
			// given
			String applicationId = "application-id";
			User admin = ObjectFixtures.getCertifiedUser();
			User applicant = ObjectFixtures.getCertifiedUser();

			UserAcademicRecordApplication application = UserAcademicRecordApplication.create(
				applicant,
				AcademicRecordRequestStatus.AWAIT,
				AcademicStatus.ENROLLED,
				null,
				"복학 신청합니다");

			when(applicationReader.findById(applicationId))
				.thenReturn(application);

			// when
			academicRecordAdminService.approve(admin, applicationId);

			// then
			verify(applicationReader).findById(applicationId);
			verify(applicationWriter).approve(application);
			verify(logCreator).createFromApplication(admin, application);
		}

		@Test
		@DisplayName("존재하지 않는 신청 승인 시 예외가 발생한다")
		void approve_notFound() {
			// given
			String applicationId = "invalid-id";
			User admin = ObjectFixtures.getCertifiedUser();

			when(applicationReader.findById(applicationId))
				.thenThrow(
					AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_FOUND
						.toBaseException());

			// when & then
			assertThatThrownBy(() -> academicRecordAdminService.approve(admin, applicationId))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(applicationWriter, never()).approve(any());
			verify(logCreator, never()).createFromApplication(any(), any());
		}

		@Test
		@DisplayName("AWAIT 상태가 아닌 신청 승인 시 예외가 발생한다")
		void approve_notAwaiting() {
			// given
			String applicationId = "application-id";
			User admin = ObjectFixtures.getCertifiedUser();
			User applicant = ObjectFixtures.getCertifiedUser();

			UserAcademicRecordApplication application = UserAcademicRecordApplication.create(
				applicant,
				AcademicRecordRequestStatus.ACCEPT,
				AcademicStatus.ENROLLED,
				null,
				"복학 신청합니다");

			when(applicationReader.findById(applicationId))
				.thenReturn(application);
			doThrow(AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_AWAITING.toBaseException())
				.when(applicationWriter).approve(application);

			// when & then
			assertThatThrownBy(() -> academicRecordAdminService.approve(admin, applicationId))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(logCreator, never()).createFromApplication(any(), any());
		}
	}

	@Nested
	@DisplayName("학적 변경 신청 반려")
	class Reject {

		@Test
		@DisplayName("학적 변경 신청을 반려한다")
		void reject_success() {
			// given
			String applicationId = "application-id";
			String rejectReason = "서류가 불충분합니다";
			User admin = ObjectFixtures.getCertifiedUser();
			User applicant = ObjectFixtures.getCertifiedUser();

			UserAcademicRecordApplication application = UserAcademicRecordApplication.create(
				applicant,
				AcademicRecordRequestStatus.AWAIT,
				AcademicStatus.ENROLLED,
				null,
				"복학 신청합니다");

			when(applicationReader.findById(applicationId))
				.thenReturn(application);

			// when
			academicRecordAdminService.reject(admin, applicationId, rejectReason);

			// then
			verify(applicationReader).findById(applicationId);
			verify(applicationWriter).reject(application, rejectReason);
			verify(logCreator).createFromApplication(admin, application);
		}

		@Test
		@DisplayName("존재하지 않는 신청 반려 시 예외가 발생한다")
		void reject_notFound() {
			// given
			String applicationId = "invalid-id";
			User admin = ObjectFixtures.getCertifiedUser();

			when(applicationReader.findById(applicationId))
				.thenThrow(
					AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_FOUND
						.toBaseException());

			// when & then
			assertThatThrownBy(() -> academicRecordAdminService.reject(admin, applicationId, "사유"))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(applicationWriter, never()).reject(any(), any());
			verify(logCreator, never()).createFromApplication(any(), any());
		}

		@Test
		@DisplayName("AWAIT 상태가 아닌 신청 반려 시 예외가 발생한다")
		void reject_notAwaiting() {
			// given
			String applicationId = "application-id";
			String rejectReason = "서류가 불충분합니다";
			User admin = ObjectFixtures.getCertifiedUser();
			User applicant = ObjectFixtures.getCertifiedUser();

			UserAcademicRecordApplication application = UserAcademicRecordApplication.create(
				applicant,
				AcademicRecordRequestStatus.REJECT,
				AcademicStatus.ENROLLED,
				null,
				"복학 신청합니다");

			when(applicationReader.findById(applicationId))
				.thenReturn(application);
			doThrow(AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_AWAITING.toBaseException())
				.when(applicationWriter).reject(application, rejectReason);

			// when & then
			assertThatThrownBy(() -> academicRecordAdminService.reject(admin, applicationId, rejectReason))
				.isInstanceOf(BaseRunTimeV2Exception.class);

			verify(logCreator, never()).createFromApplication(any(), any());
		}
	}
}
