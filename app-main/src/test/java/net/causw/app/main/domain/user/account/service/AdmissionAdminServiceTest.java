package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.academic.event.CertifiedUserCreatedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionListCondition;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionResult;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionLogWriter;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionValidator;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class AdmissionAdminServiceTest {

	@Mock
	private AdmissionReader admissionReader;

	@Mock
	private AdmissionValidator admissionValidator;

	@Mock
	private AdmissionWriter admissionWriter;

	@Mock
	private AdmissionLogWriter admissionLogWriter;

	@Mock
	private UserWriter userWriter;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private AdmissionAdminService admissionAdminService;

	/* =========================
	 * getAdmissionList 테스트
	 * ========================= */
	@Nested
	@DisplayName("getAdmissionList - 관리자 재학인증 신청 목록 조회")
	class GetAdmissionList {

		@Test
		@DisplayName("조건 없이 전체 신청 목록을 조회한다")
		void givenNoCondition_whenGetAdmissionList_thenReturnAllAdmissions() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition(null, null);
			Pageable pageable = PageRequest.of(0, 10);

			User user1 = ObjectFixtures.getUserWithId("user-1");
			User user2 = ObjectFixtures.getUserWithId("user-2");
			UserAdmission admission1 = ObjectFixtures.getUserAdmissionWithId("admission-1", user1);
			UserAdmission admission2 = ObjectFixtures.getUserAdmissionWithId("admission-2", user2);

			Page<UserAdmission> admissionPage = new PageImpl<>(
				List.of(admission1, admission2), pageable, 2);

			when(admissionReader.findAdmissionList(null, null, pageable))
				.thenReturn(admissionPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getTotalElements()).isEqualTo(2);
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getContent().get(0).id()).isEqualTo("admission-1");
			assertThat(result.getContent().get(1).id()).isEqualTo("admission-2");

			verify(admissionReader).findAdmissionList(null, null, pageable);
		}

		@Test
		@DisplayName("키워드로 필터링하여 신청 목록을 조회한다")
		void givenKeyword_whenGetAdmissionList_thenReturnFilteredAdmissions() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition("name", null);
			Pageable pageable = PageRequest.of(0, 10);

			User user = ObjectFixtures.getUserWithId("user-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId("admission-1", user);

			Page<UserAdmission> admissionPage = new PageImpl<>(
				List.of(admission), pageable, 1);

			when(admissionReader.findAdmissionList("name", null, pageable))
				.thenReturn(admissionPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).userName()).isEqualTo(user.getName());

			verify(admissionReader).findAdmissionList("name", null, pageable);
		}

		@Test
		@DisplayName("사용자 상태로 필터링하여 신청 목록을 조회한다")
		void givenUserState_whenGetAdmissionList_thenReturnFilteredAdmissions() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition(null, UserState.AWAIT);
			Pageable pageable = PageRequest.of(0, 10);

			User user = ObjectFixtures.getUserWithId("user-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId("admission-1", user);

			Page<UserAdmission> admissionPage = new PageImpl<>(
				List.of(admission), pageable, 1);

			when(admissionReader.findAdmissionList(null, UserState.AWAIT, pageable))
				.thenReturn(admissionPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).userState()).isEqualTo(UserState.AWAIT);

			verify(admissionReader).findAdmissionList(null, UserState.AWAIT, pageable);
		}

		@Test
		@DisplayName("결과가 없으면 빈 페이지를 반환한다")
		void givenNoResult_whenGetAdmissionList_thenReturnEmptyPage() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition("없는이름", null);
			Pageable pageable = PageRequest.of(0, 10);

			Page<UserAdmission> emptyPage = new PageImpl<>(List.of(), pageable, 0);

			when(admissionReader.findAdmissionList("없는이름", null, pageable))
				.thenReturn(emptyPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getTotalElements()).isZero();
			assertThat(result.getContent()).isEmpty();

			verify(admissionReader).findAdmissionList("없는이름", null, pageable);
		}

		@Test
		@DisplayName("AdmissionResult로 올바르게 매핑되는지 확인한다")
		void givenAdmission_whenGetAdmissionList_thenResultFieldsMappedCorrectly() {
			// given
			AdmissionListCondition condition = new AdmissionListCondition(null, null);
			Pageable pageable = PageRequest.of(0, 10);

			User user = ObjectFixtures.getUserWithId("user-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId("admission-1", user);

			Page<UserAdmission> admissionPage = new PageImpl<>(
				List.of(admission), pageable, 1);

			when(admissionReader.findAdmissionList(null, null, pageable))
				.thenReturn(admissionPage);

			// when
			Page<AdmissionResult> result = admissionAdminService.getAdmissionList(condition, pageable);

			// then
			AdmissionResult item = result.getContent().get(0);
			assertThat(item.id()).isEqualTo("admission-1");
			assertThat(item.userName()).isEqualTo(user.getName());
			assertThat(item.userEmail()).isEqualTo(user.getEmail());
			assertThat(item.requestedDepartment()).isEqualTo(Department.SCHOOL_OF_SW);
			assertThat(item.requestedAdmissionYear()).isEqualTo(2023);
			assertThat(item.requestedStudentId()).isEqualTo("20231234");
			assertThat(item.requestedAcademicStatus()).isEqualTo(AcademicStatus.ENROLLED);
			assertThat(item.description()).isEqualTo("재학증명서 첨부합니다");
			assertThat(item.attachImageUrls()).hasSize(1);
			assertThat(item.userState()).isEqualTo(UserState.AWAIT);
		}
	}

	/* =========================
	 * getAdmissionDetail 테스트
	 * ========================= */
	@Nested
	@DisplayName("getAdmissionDetail - 관리자 재학인증 신청 상세 조회")
	class GetAdmissionDetail {

		@Test
		@DisplayName("존재하는 신청 ID로 상세를 조회한다")
		void givenValidAdmissionId_whenGetAdmissionDetail_thenReturnAdmissionResult() {
			// given
			String admissionId = "admission-1";
			User user = ObjectFixtures.getUserWithId("user-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId(admissionId, user);

			when(admissionReader.findAdmissionDetail(admissionId)).thenReturn(admission);

			// when
			AdmissionResult result = admissionAdminService.getAdmissionDetail(admissionId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo(admissionId);
			assertThat(result.userName()).isEqualTo(user.getName());
			assertThat(result.userEmail()).isEqualTo(user.getEmail());
			assertThat(result.requestedDepartment()).isEqualTo(Department.SCHOOL_OF_SW);
			assertThat(result.requestedAdmissionYear()).isEqualTo(2023);
			assertThat(result.requestedStudentId()).isEqualTo("20231234");
			assertThat(result.requestedAcademicStatus()).isEqualTo(AcademicStatus.ENROLLED);
			assertThat(result.description()).isEqualTo("재학증명서 첨부합니다");
			assertThat(result.attachImageUrls()).hasSize(1);
			assertThat(result.attachImageUrls().get(0)).contains("storage.example.com");
			assertThat(result.userState()).isEqualTo(UserState.AWAIT);

			verify(admissionReader).findAdmissionDetail(admissionId);
		}

		@Test
		@DisplayName("존재하지 않는 신청 ID로 조회하면 ADMISSION_NOT_FOUND 예외가 발생한다")
		void givenInvalidAdmissionId_whenGetAdmissionDetail_thenThrowNotFoundException() {
			// given
			String admissionId = "non-existent-id";

			when(admissionReader.findAdmissionDetail(admissionId))
				.thenThrow(UserErrorCode.ADMISSION_NOT_FOUND.toBaseException());

			// when & then
			assertThatThrownBy(() -> admissionAdminService.getAdmissionDetail(admissionId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.ADMISSION_NOT_FOUND);

			verify(admissionReader).findAdmissionDetail(admissionId);
		}
	}

	/* =========================
	 * approveAdmission 테스트
	 * ========================= */
	@Nested
	@DisplayName("approveAdmission - 재학인증 신청 승인")
	class ApproveAdmission {

		@Test
		@DisplayName("정상적으로 승인하면 중복 검증 → 유저 정보 업데이트 → 이벤트 발행 → 로그 생성 → 신청 삭제 순서로 실행된다")
		void givenValidAdmission_whenApprove_thenAllStepsExecutedInOrder() {
			// given
			String admissionId = "admission-1";
			User targetUser = ObjectFixtures.getUserWithId("user-1");
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId(admissionId, targetUser);

			when(admissionReader.findAdmissionDetail(admissionId)).thenReturn(admission);
			when(userWriter.approveAdmission(targetUser, admission)).thenReturn(targetUser);

			// when
			admissionAdminService.approveAdmission(admissionId, adminUser);

			// then
			verify(admissionReader).findAdmissionDetail(admissionId);
			verify(admissionValidator).validateStudentIdNotDuplicated(admission.getRequestedStudentId());
			verify(userWriter).approveAdmission(targetUser, admission);
			verify(admissionLogWriter).createAcceptLog(admission, adminUser);
			verify(admissionWriter).delete(admission);
		}

		@Test
		@DisplayName("승인 시 CertifiedUserCreatedEvent가 대상 유저 ID로 발행된다")
		void givenValidAdmission_whenApprove_thenCertifiedUserCreatedEventPublished() {
			// given
			String admissionId = "admission-1";
			User targetUser = ObjectFixtures.getUserWithId("user-1");
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId(admissionId, targetUser);

			when(admissionReader.findAdmissionDetail(admissionId)).thenReturn(admission);
			when(userWriter.approveAdmission(targetUser, admission)).thenReturn(targetUser);

			// when
			admissionAdminService.approveAdmission(admissionId, adminUser);

			// then
			ArgumentCaptor<CertifiedUserCreatedEvent> eventCaptor = ArgumentCaptor
				.forClass(CertifiedUserCreatedEvent.class);
			verify(eventPublisher).publishEvent(eventCaptor.capture());

			CertifiedUserCreatedEvent capturedEvent = eventCaptor.getValue();
			assertThat(capturedEvent.userId()).isEqualTo("user-1");
		}

		@Test
		@DisplayName("학번 중복 검증에 실패하면 예외가 발생하고 이후 단계가 실행되지 않는다")
		void givenDuplicateStudentId_whenApprove_thenThrowAndStopProcess() {
			// given
			String admissionId = "admission-1";
			User targetUser = ObjectFixtures.getUserWithId("user-1");
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId(admissionId, targetUser);

			when(admissionReader.findAdmissionDetail(admissionId)).thenReturn(admission);
			doThrow(UserErrorCode.STUDENT_ID_ALREADY_EXIST.toBaseException())
				.when(admissionValidator).validateStudentIdNotDuplicated(admission.getRequestedStudentId());

			// when & then
			assertThatThrownBy(() -> admissionAdminService.approveAdmission(admissionId, adminUser))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.STUDENT_ID_ALREADY_EXIST);

			verify(userWriter, never()).approveAdmission(targetUser, admission);
			verifyNoInteractions(eventPublisher);
			verify(admissionLogWriter, never()).createAcceptLog(admission, adminUser);
			verify(admissionWriter, never()).delete(admission);
		}

		@Test
		@DisplayName("존재하지 않는 신청 ID로 승인하면 ADMISSION_NOT_FOUND 예외가 발생한다")
		void givenNonExistentAdmissionId_whenApprove_thenThrowNotFoundException() {
			// given
			String admissionId = "non-existent-id";
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");

			when(admissionReader.findAdmissionDetail(admissionId))
				.thenThrow(UserErrorCode.ADMISSION_NOT_FOUND.toBaseException());

			// when & then
			assertThatThrownBy(() -> admissionAdminService.approveAdmission(admissionId, adminUser))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.ADMISSION_NOT_FOUND);

			verifyNoInteractions(admissionValidator);
			verifyNoInteractions(userWriter);
			verifyNoInteractions(eventPublisher);
			verifyNoInteractions(admissionLogWriter);
			verifyNoInteractions(admissionWriter);
		}
	}

	/* =========================
	 * rejectAdmission 테스트
	 * ========================= */
	@Nested
	@DisplayName("rejectAdmission - 재학인증 신청 거절")
	class RejectAdmission {

		@Test
		@DisplayName("정상적으로 거절하면 유저 상태 변경 → 로그 생성 → 신청 삭제 순서로 실행된다")
		void givenValidRejectReason_whenReject_thenAllStepsExecutedInOrder() {
			// given
			String admissionId = "admission-1";
			String rejectReason = "서류가 불명확합니다";
			User targetUser = ObjectFixtures.getUserWithId("user-1");
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId(admissionId, targetUser);

			when(admissionReader.findAdmissionDetail(admissionId)).thenReturn(admission);
			when(userWriter.rejectAdmission(targetUser, rejectReason)).thenReturn(targetUser);

			// when
			admissionAdminService.rejectAdmission(admissionId, adminUser, rejectReason);

			// then
			verify(admissionReader).findAdmissionDetail(admissionId);
			verify(userWriter).rejectAdmission(targetUser, rejectReason);
			verify(admissionLogWriter).createRejectLog(admission, adminUser, rejectReason);
			verify(admissionWriter).delete(admission);
		}

		@Test
		@DisplayName("거절 시 이벤트가 발행되지 않는다")
		void givenRejectAdmission_whenReject_thenNoEventPublished() {
			// given
			String admissionId = "admission-1";
			String rejectReason = "서류가 불명확합니다";
			User targetUser = ObjectFixtures.getUserWithId("user-1");
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId(admissionId, targetUser);

			when(admissionReader.findAdmissionDetail(admissionId)).thenReturn(admission);
			when(userWriter.rejectAdmission(targetUser, rejectReason)).thenReturn(targetUser);

			// when
			admissionAdminService.rejectAdmission(admissionId, adminUser, rejectReason);

			// then
			verifyNoInteractions(eventPublisher);
		}

		@ParameterizedTest
		@NullAndEmptySource
		@DisplayName("거절 사유가 null 또는 빈 문자열이면 ADMISSION_REJECT_REASON_REQUIRED 예외가 발생한다")
		void givenNullOrEmptyReason_whenReject_thenThrowException(String rejectReason) {
			// given
			String admissionId = "admission-1";
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");

			// when & then
			assertThatThrownBy(
				() -> admissionAdminService.rejectAdmission(admissionId, adminUser, rejectReason))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.ADMISSION_REJECT_REASON_REQUIRED);

			verifyNoInteractions(admissionReader);
			verifyNoInteractions(userWriter);
			verifyNoInteractions(admissionLogWriter);
			verifyNoInteractions(admissionWriter);
		}

		@ParameterizedTest
		@ValueSource(strings = {"   ", "\t", "\n", "  \t\n  "})
		@DisplayName("거절 사유가 공백 문자만으로 이루어져 있으면 ADMISSION_REJECT_REASON_REQUIRED 예외가 발생한다")
		void givenBlankReason_whenReject_thenThrowException(String rejectReason) {
			// given
			String admissionId = "admission-1";
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");

			// when & then
			assertThatThrownBy(
				() -> admissionAdminService.rejectAdmission(admissionId, adminUser, rejectReason))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.ADMISSION_REJECT_REASON_REQUIRED);

			verifyNoInteractions(admissionReader);
		}

		@Test
		@DisplayName("존재하지 않는 신청 ID로 거절하면 ADMISSION_NOT_FOUND 예외가 발생한다")
		void givenNonExistentAdmissionId_whenReject_thenThrowNotFoundException() {
			// given
			String admissionId = "non-existent-id";
			String rejectReason = "서류가 불명확합니다";
			User adminUser = ObjectFixtures.getCertifiedUserWithId("admin-1");

			when(admissionReader.findAdmissionDetail(admissionId))
				.thenThrow(UserErrorCode.ADMISSION_NOT_FOUND.toBaseException());

			// when & then
			assertThatThrownBy(
				() -> admissionAdminService.rejectAdmission(admissionId, adminUser, rejectReason))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.ADMISSION_NOT_FOUND);

			verifyNoInteractions(userWriter);
			verifyNoInteractions(admissionLogWriter);
			verifyNoInteractions(admissionWriter);
		}
	}
}
