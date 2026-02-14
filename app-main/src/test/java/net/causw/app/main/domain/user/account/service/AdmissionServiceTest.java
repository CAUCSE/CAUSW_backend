package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
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
import net.causw.app.main.shared.storage.v2.dto.FileMetadata;
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

	/* =========================
	 * Helper Methods
	 * ========================= */

	private User createAwaitUser(String userId) {
		User user = ObjectFixtures.getUser(); // state = AWAIT
		ReflectionTestUtils.setField(user, "id", userId);
		return user;
	}

	private User createRejectUser(String userId) {
		User user = ObjectFixtures.getUser();
		ReflectionTestUtils.setField(user, "id", userId);
		user.setState(UserState.REJECT);
		return user;
	}

	private User createActiveUser(String userId) {
		return ObjectFixtures.getCertifiedUserWithId(userId);
	}

	private AdmissionCreateCommand createCommand() {
		return AdmissionCreateCommand.builder()
			.description("재학증명서 첨부합니다")
			.targetAcademicStatus(AcademicStatus.ENROLLED)
			.studentId("20231234")
			.admissionYear(2023)
			.department(Department.SCHOOL_OF_SW)
			.build();
	}

	private List<MultipartFile> createAttachImages() {
		MockMultipartFile image = new MockMultipartFile(
			"attachImages",
			"test-image.png",
			"image/png",
			"image-content".getBytes());
		return List.of(image);
	}

	private UuidFile createUuidFile() {
		UuidFile uuidFile = UuidFile.of(
			"test-uuid",
			"user-admission/test-image_test-uuid.png",
			"https://storage.example.com/user-admission/test-image_test-uuid.png",
			"test-image",
			"png",
			FilePath.USER_ADMISSION);
		ReflectionTestUtils.setField(uuidFile, "id", "uuid-file-1");
		return uuidFile;
	}

	private UserAdmission createUserAdmission(User user, List<UuidFile> uuidFiles, AdmissionCreateCommand command) {
		UserAdmission admission = UserAdmission.of(
			user,
			uuidFiles,
			command.description(),
			command.targetAcademicStatus(),
			command.studentId(),
			command.admissionYear(),
			command.department());
		ReflectionTestUtils.setField(admission, "id", "admission-1");
		return admission;
	}

	/* =========================
	 * createAdmission 테스트
	 * ========================= */
	@Nested
	@DisplayName("createAdmission - 재학정보 인증 신청 생성")
	class CreateAdmission {

		@Test
		@DisplayName("AWAIT 상태의 사용자가 유효한 요청을 하면 인증 신청이 생성된다")
		void givenAwaitUser_whenCreateAdmission_thenSuccess() {
			// given
			String userId = "user-1";
			User user = createAwaitUser(userId);
			AdmissionCreateCommand command = createCommand();
			List<MultipartFile> attachImages = createAttachImages();
			UuidFile uuidFile = createUuidFile();
			List<UuidFile> uuidFiles = List.of(uuidFile);
			UserAdmission admission = createUserAdmission(user, uuidFiles, command);

			doNothing().when(admissionValidator).validateAdmissionCreate(user, attachImages);
			fileValidatorMock.when(() -> FileValidator.validateFileList(attachImages, FilePath.USER_ADMISSION))
				.then(invocation -> null);
			fileMetadataManagerMock.when(
					() -> FileMetadataManager.createMetadata(any(MultipartFile.class), eq(FilePath.USER_ADMISSION)))
				.thenReturn(FileMetadata.of(
					"test-uuid", "test-image", "png", "test-image.png",
					FilePath.USER_ADMISSION, "user-admission/test-image_test-uuid.png"));

			when(fileWriter.uploadAndSaveList(anyList(), anyList())).thenReturn(uuidFiles);
			when(userWriter.updateStateToAwait(user)).thenReturn(user);
			when(admissionWriter.create(
				eq(user), eq(uuidFiles), eq(command.description()),
				eq(command.targetAcademicStatus()), eq(command.studentId()),
				eq(command.admissionYear()), eq(command.department())))
				.thenReturn(admission);

			// when
			AdmissionResult result = admissionService.createAdmission(user, command, attachImages);

			// then
			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo("admission-1");
			assertThat(result.userName()).isEqualTo(user.getName());
			assertThat(result.userEmail()).isEqualTo(user.getEmail());
			assertThat(result.department()).isEqualTo(Department.SCHOOL_OF_SW);
			assertThat(result.admissionYear()).isEqualTo(2023);
			assertThat(result.studentId()).isEqualTo("20231234");
			assertThat(result.targetAcademicStatus()).isEqualTo(AcademicStatus.ENROLLED);
			assertThat(result.description()).isEqualTo("재학증명서 첨부합니다");

			verify(admissionValidator).validateAdmissionCreate(user, attachImages);
			verify(fileWriter).uploadAndSaveList(anyList(), anyList());
			verify(userWriter).updateStateToAwait(user);
			verify(admissionWriter).create(
				eq(user), eq(uuidFiles), eq(command.description()),
				eq(command.targetAcademicStatus()), eq(command.studentId()),
				eq(command.admissionYear()), eq(command.department()));
		}

		@Test
		@DisplayName("REJECT 상태의 사용자가 재신청하면 인증 신청이 생성된다")
		void givenRejectUser_whenCreateAdmission_thenSuccess() {
			// given
			String userId = "user-2";
			User user = createRejectUser(userId);
			AdmissionCreateCommand command = createCommand();
			List<MultipartFile> attachImages = createAttachImages();
			UuidFile uuidFile = createUuidFile();
			List<UuidFile> uuidFiles = List.of(uuidFile);
			UserAdmission admission = createUserAdmission(user, uuidFiles, command);

			doNothing().when(admissionValidator).validateAdmissionCreate(user, attachImages);
			fileValidatorMock.when(() -> FileValidator.validateFileList(attachImages, FilePath.USER_ADMISSION))
				.then(invocation -> null);
			fileMetadataManagerMock.when(
					() -> FileMetadataManager.createMetadata(any(MultipartFile.class), eq(FilePath.USER_ADMISSION)))
				.thenReturn(FileMetadata.of(
					"test-uuid", "test-image", "png", "test-image.png",
					FilePath.USER_ADMISSION, "user-admission/test-image_test-uuid.png"));

			when(fileWriter.uploadAndSaveList(anyList(), anyList())).thenReturn(uuidFiles);
			when(userWriter.updateStateToAwait(user)).thenReturn(user);
			when(admissionWriter.create(
				eq(user), eq(uuidFiles), eq(command.description()),
				eq(command.targetAcademicStatus()), eq(command.studentId()),
				eq(command.admissionYear()), eq(command.department())))
				.thenReturn(admission);

			// when
			AdmissionResult result = admissionService.createAdmission(user, command, attachImages);

			// then
			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo("admission-1");

			verify(userWriter).updateStateToAwait(user);
			verify(admissionWriter).create(
				eq(user), eq(uuidFiles), eq(command.description()),
				eq(command.targetAcademicStatus()), eq(command.studentId()),
				eq(command.admissionYear()), eq(command.department()));
		}

		@Test
		@DisplayName("사용자 상태가 AWAIT/REJECT가 아니면 예외가 발생한다")
		void givenActiveUser_whenCreateAdmission_thenThrowInvalidStateException() {
			// given
			User user = createActiveUser("user-3");
			AdmissionCreateCommand command = createCommand();
			List<MultipartFile> attachImages = createAttachImages();

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
			User user = createAwaitUser("user-4");
			AdmissionCreateCommand command = createCommand();
			List<MultipartFile> attachImages = createAttachImages();

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
			User user = createAwaitUser("user-5");
			AdmissionCreateCommand command = createCommand();
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
			User user = createAwaitUser(userId);

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
			User user = createAwaitUser(userId);

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
			User user = createRejectUser(userId);
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
			User user = createActiveUser(userId);

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
