package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileWriter;
import net.causw.app.main.domain.asset.file.service.v2.util.FileMetadataManager;
import net.causw.app.main.domain.asset.file.service.v2.util.FileValidator;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionCreateCommand;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionResult;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionStateResult;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionValidator;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class AdmissionServiceTest {

	@Mock
	private AdmissionReader admissionReader;

	@Mock
	private AdmissionValidator admissionValidator;

	@Mock
	private AdmissionWriter admissionWriter;

	@Mock
	private UserWriter userWriter;

	@Mock
	private FileWriter fileWriter;

	@InjectMocks
	private AdmissionService admissionService;

	/* =========================
	 * createAdmission 테스트
	 * ========================= */
	@Nested
	@DisplayName("createAdmission - 재학정보 인증 신청 생성")
	class CreateAdmission {

		private MockedStatic<FileValidator> fileValidatorMock;
		private MockedStatic<FileMetadataManager> fileMetadataManagerMock;

		@BeforeEach
		void setUp() {
			fileValidatorMock = mockStatic(FileValidator.class);
			fileMetadataManagerMock = mockStatic(FileMetadataManager.class);
		}

		@AfterEach
		void tearDown() {
			fileValidatorMock.close();
			fileMetadataManagerMock.close();
		}

		@Test
		@DisplayName("AWAIT 상태의 사용자가 유효한 요청을 하면 인증 신청이 생성된다")
		void givenAwaitUser_whenCreateAdmission_thenSuccess() {
			// given
			User user = ObjectFixtures.getUserWithId("user-1");
			AdmissionCreateCommand command = ObjectFixtures.getAdmissionCreateCommand();
			List<MultipartFile> attachImages = ObjectFixtures.getMockAttachImages();
			List<UuidFile> uuidFiles = List.of(ObjectFixtures.getUuidFile());
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId("admission-1", user);

			when(fileWriter.uploadAndSaveList(anyList(), anyList())).thenReturn(uuidFiles);
			when(userWriter.updateStateToAwait(user)).thenReturn(user);
			when(admissionWriter.create(
				eq(user), eq(uuidFiles), eq(command.description()),
				eq(command.requestedAcademicStatus()), eq(command.requestedStudentId()),
				eq(command.requestedAdmissionYear()), eq(command.requestedDepartment())))
				.thenReturn(admission);

			// when
			AdmissionResult result = admissionService.createAdmission(user, command, attachImages);

			// then
			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo("admission-1");
			assertThat(result.userName()).isEqualTo(user.getName());
			assertThat(result.userEmail()).isEqualTo(user.getEmail());
			assertThat(result.requestedDepartment()).isEqualTo(Department.SCHOOL_OF_SW);
			assertThat(result.requestedAdmissionYear()).isEqualTo(2023);
			assertThat(result.requestedStudentId()).isEqualTo("20231234");
			assertThat(result.requestedAcademicStatus()).isEqualTo(AcademicStatus.ENROLLED);
			assertThat(result.description()).isEqualTo("재학증명서 첨부합니다");

			verify(admissionValidator).validateAdmissionCreate(user, attachImages);
			verify(fileWriter).uploadAndSaveList(anyList(), anyList());
			verify(userWriter).updateStateToAwait(user);
			verify(admissionWriter).create(
				eq(user), eq(uuidFiles), eq(command.description()),
				eq(command.requestedAcademicStatus()), eq(command.requestedStudentId()),
				eq(command.requestedAdmissionYear()), eq(command.requestedDepartment()));
		}

		@Test
		@DisplayName("REJECT 상태의 사용자가 재신청하면 인증 신청이 생성된다")
		void givenRejectUser_whenCreateAdmission_thenSuccess() {
			// given
			User user = ObjectFixtures.getRejectUserWithId("user-2");
			AdmissionCreateCommand command = ObjectFixtures.getAdmissionCreateCommand();
			List<MultipartFile> attachImages = ObjectFixtures.getMockAttachImages();
			List<UuidFile> uuidFiles = List.of(ObjectFixtures.getUuidFile());
			UserAdmission admission = ObjectFixtures.getUserAdmissionWithId("admission-1", user);

			when(fileWriter.uploadAndSaveList(anyList(), anyList())).thenReturn(uuidFiles);
			when(userWriter.updateStateToAwait(user)).thenReturn(user);
			when(admissionWriter.create(
				eq(user), eq(uuidFiles), eq(command.description()),
				eq(command.requestedAcademicStatus()), eq(command.requestedStudentId()),
				eq(command.requestedAdmissionYear()), eq(command.requestedDepartment())))
				.thenReturn(admission);

			// when
			AdmissionResult result = admissionService.createAdmission(user, command, attachImages);

			// then
			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo("admission-1");

			verify(userWriter).updateStateToAwait(user);
			verify(admissionWriter).create(
				eq(user), eq(uuidFiles), eq(command.description()),
				eq(command.requestedAcademicStatus()), eq(command.requestedStudentId()),
				eq(command.requestedAdmissionYear()), eq(command.requestedDepartment()));
		}

		@Test
		@DisplayName("사용자 상태가 AWAIT/REJECT가 아니면 예외가 발생한다")
		void givenActiveUser_whenCreateAdmission_thenThrowInvalidStateException() {
			// given
			User user = ObjectFixtures.getCertifiedUserWithId("user-3");
			AdmissionCreateCommand command = ObjectFixtures.getAdmissionCreateCommand();
			List<MultipartFile> attachImages = ObjectFixtures.getMockAttachImages();

			doThrow(UserErrorCode.INVALID_USER_STATE_FOR_ADMISSION.toBaseException())
				.when(admissionValidator).validateAdmissionCreate(user, attachImages);

			// when & then
			assertThatThrownBy(() -> admissionService.createAdmission(user, command, attachImages))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.INVALID_USER_STATE_FOR_ADMISSION);

			verify(admissionValidator).validateAdmissionCreate(user, attachImages);
			verify(fileWriter, never()).uploadAndSaveList(anyList(), anyList());
			verify(admissionWriter, never()).create(any(), any(), any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("이미 인증 신청이 존재하면 예외가 발생한다")
		void givenExistingAdmission_whenCreateAdmission_thenThrowDuplicateException() {
			// given
			User user = ObjectFixtures.getUserWithId("user-4");
			AdmissionCreateCommand command = ObjectFixtures.getAdmissionCreateCommand();
			List<MultipartFile> attachImages = ObjectFixtures.getMockAttachImages();

			doThrow(UserErrorCode.ADMISSION_ALREADY_EXISTS.toBaseException())
				.when(admissionValidator).validateAdmissionCreate(user, attachImages);

			// when & then
			assertThatThrownBy(() -> admissionService.createAdmission(user, command, attachImages))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.ADMISSION_ALREADY_EXISTS);

			verify(admissionValidator).validateAdmissionCreate(user, attachImages);
			verify(fileWriter, never()).uploadAndSaveList(anyList(), anyList());
			verify(admissionWriter, never()).create(any(), any(), any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("첨부 이미지가 없으면 예외가 발생한다")
		void givenNoAttachImages_whenCreateAdmission_thenThrowImageRequiredException() {
			// given
			User user = ObjectFixtures.getUserWithId("user-5");
			AdmissionCreateCommand command = ObjectFixtures.getAdmissionCreateCommand();
			List<MultipartFile> emptyImages = List.of();

			doThrow(UserErrorCode.ADMISSION_IMAGE_REQUIRED.toBaseException())
				.when(admissionValidator).validateAdmissionCreate(user, emptyImages);

			// when & then
			assertThatThrownBy(() -> admissionService.createAdmission(user, command, emptyImages))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.ADMISSION_IMAGE_REQUIRED);

			verify(admissionValidator).validateAdmissionCreate(user, emptyImages);
			verify(fileWriter, never()).uploadAndSaveList(anyList(), anyList());
			verify(admissionWriter, never()).create(any(), any(), any(), any(), any(), any(), any());
		}

	}

	/* =========================
	 * getAdmissionState 테스트
	 * ========================= */
	@Nested
	@DisplayName("getAdmissionState - 인증 신청 상태 조회")
	class GetAdmissionState {

		@Test
		@DisplayName("인증 신청이 존재하는 사용자의 상태를 조회한다")
		void givenUserWithAdmission_whenGetAdmissionState_thenReturnStateWithAdmission() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.getUserWithId(userId);

			when(admissionReader.existsByUserId(userId)).thenReturn(true);

			// when
			AdmissionStateResult result = admissionService.getAdmissionState(user);

			// then
			assertThat(result).isNotNull();
			assertThat(result.userState()).isEqualTo(UserState.AWAIT);
			assertThat(result.hasAdmission()).isTrue();
			assertThat(result.rejectReason()).isNull();

			verify(admissionReader).existsByUserId(userId);
		}

		@Test
		@DisplayName("인증 신청이 없는 사용자의 상태를 조회한다")
		void givenUserWithoutAdmission_whenGetAdmissionState_thenReturnStateWithoutAdmission() {
			// given
			String userId = "user-2";
			User user = ObjectFixtures.getUserWithId(userId);

			when(admissionReader.existsByUserId(userId)).thenReturn(false);

			// when
			AdmissionStateResult result = admissionService.getAdmissionState(user);

			// then
			assertThat(result).isNotNull();
			assertThat(result.userState()).isEqualTo(UserState.AWAIT);
			assertThat(result.hasAdmission()).isFalse();

			verify(admissionReader).existsByUserId(userId);
		}

		@Test
		@DisplayName("REJECT 상태 사용자의 상태를 조회하면 거절 사유가 포함된다")
		void givenRejectUser_whenGetAdmissionState_thenReturnStateWithRejectReason() {
			// given
			String userId = "user-3";
			User user = ObjectFixtures.getRejectUserWithId(userId);
			user.updateRejectionOrDropReason("서류 불충분");

			when(admissionReader.existsByUserId(userId)).thenReturn(false);

			// when
			AdmissionStateResult result = admissionService.getAdmissionState(user);

			// then
			assertThat(result).isNotNull();
			assertThat(result.userState()).isEqualTo(UserState.REJECT);
			assertThat(result.hasAdmission()).isFalse();
			assertThat(result.rejectReason()).isEqualTo("서류 불충분");

			verify(admissionReader).existsByUserId(userId);
		}

		@Test
		@DisplayName("ACTIVE 상태 사용자의 상태를 조회한다")
		void givenActiveUser_whenGetAdmissionState_thenReturnActiveState() {
			// given
			String userId = "user-4";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);

			when(admissionReader.existsByUserId(userId)).thenReturn(false);

			// when
			AdmissionStateResult result = admissionService.getAdmissionState(user);

			// then
			assertThat(result).isNotNull();
			assertThat(result.userState()).isEqualTo(UserState.ACTIVE);
			assertThat(result.hasAdmission()).isFalse();

			verify(admissionReader).existsByUserId(userId);
		}
	}
}
