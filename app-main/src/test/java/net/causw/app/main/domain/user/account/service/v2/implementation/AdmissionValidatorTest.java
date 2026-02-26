package net.causw.app.main.domain.user.account.service.v2.implementation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionReader;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionValidator;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class AdmissionValidatorTest {

	@Mock
	private AdmissionReader admissionReader;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AdmissionValidator admissionValidator;

	@Nested
	@DisplayName("validateAdmissionCreate")
	class ValidateAdmissionCreate {

		@Test
		@DisplayName("모든 조건을 만족하면 예외 없이 통과한다")
		void givenValidInput_whenValidate_thenSuccess() {
			// given
			User user = ObjectFixtures.getUserWithId("user-1");
			List<MultipartFile> attachImages = ObjectFixtures.getMockAttachImages();

			when(admissionReader.existsByUserId(user.getId())).thenReturn(false);
			when(userRepository.findByStudentId("20231234")).thenReturn(Optional.empty());

			// when & then
			assertThatCode(() -> admissionValidator.validateAdmissionCreate(
				user,
				"20231234",
				AcademicStatus.ENROLLED,
				null,
				attachImages)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("validateUserStateForAdmission")
	class ValidateUserStateForAdmission {

		@Test
		@DisplayName("AWAIT 상태는 통과한다")
		void givenAwaitState_whenValidate_thenSuccess() {
			User user = ObjectFixtures.getUserWithId("user-1");

			assertThatCode(() -> admissionValidator.validateUserStateForAdmission(user))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("ACTIVE 상태면 INVALID_USER_STATE_FOR_ADMISSION 예외가 발생한다")
		void givenActiveState_whenValidate_thenThrow() {
			User user = ObjectFixtures.getCertifiedUserWithId("user-2");

			assertThatThrownBy(() -> admissionValidator.validateUserStateForAdmission(user))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.INVALID_USER_STATE_FOR_ADMISSION);
		}
	}

	@Test
	@DisplayName("기존 신청이 있으면 ADMISSION_ALREADY_EXISTS 예외가 발생한다")
	void givenExistingAdmission_whenValidateNoExistingAdmission_thenThrow() {
		User user = ObjectFixtures.getUserWithId("user-3");
		when(admissionReader.existsByUserId(user.getId())).thenReturn(true);

		assertThatThrownBy(() -> admissionValidator.validateNoExistingAdmission(user))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
			.isEqualTo(UserErrorCode.ADMISSION_ALREADY_EXISTS);
	}

	@Test
	@DisplayName("첨부 이미지가 없으면 ADMISSION_IMAGE_REQUIRED 예외가 발생한다")
	void givenEmptyAttachImages_whenValidateAttachImages_thenThrow() {
		assertThatThrownBy(() -> admissionValidator.validateAttachImages(List.of()))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
			.isEqualTo(UserErrorCode.ADMISSION_IMAGE_REQUIRED);
	}

	@Nested
	@DisplayName("validateGraduationYear")
	class ValidateGraduationYear {

		@Test
		@DisplayName("GRADUATED인데 졸업연도가 없으면 GRADUATION_YEAR_REQUIRED 예외가 발생한다")
		void givenGraduatedWithoutYear_whenValidate_thenThrow() {
			assertThatThrownBy(() -> admissionValidator.validateGraduationYear(AcademicStatus.GRADUATED, null))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.GRADUATION_YEAR_REQUIRED);
		}

		@Test
		@DisplayName("ENROLLED이면 졸업연도가 없어도 통과한다")
		void givenEnrolledWithoutYear_whenValidate_thenSuccess() {
			assertThatCode(() -> admissionValidator.validateGraduationYear(AcademicStatus.ENROLLED, null))
				.doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("validateStudentIdNotDuplicated")
	class ValidateStudentIdNotDuplicated {

		@Test
		@DisplayName("학번이 ACTIVE 사용자에게 이미 있으면 STUDENT_ID_ALREADY_EXIST 예외가 발생한다")
		void givenDuplicatedStudentIdInActiveUser_whenValidate_thenThrow() {
			User existingUser = ObjectFixtures.getCertifiedUserWithId("existing-user");
			when(userRepository.findByStudentId("20231234")).thenReturn(Optional.of(existingUser));

			assertThatThrownBy(() -> admissionValidator.validateStudentIdNotDuplicated("20231234"))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.STUDENT_ID_ALREADY_EXIST);
		}

		@Test
		@DisplayName("학번이 AWAIT 사용자에게 있으면 통과한다")
		void givenDuplicatedStudentIdInAwaitUser_whenValidate_thenSuccess() {
			User existingUser = ObjectFixtures.getUserWithId("await-user");
			existingUser.setState(UserState.AWAIT);
			when(userRepository.findByStudentId("20231234")).thenReturn(Optional.of(existingUser));

			assertThatCode(() -> admissionValidator.validateStudentIdNotDuplicated("20231234"))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("학번이 null이면 중복 조회를 수행하지 않는다")
		void givenNullStudentId_whenValidate_thenSkip() {
			assertThatCode(() -> admissionValidator.validateStudentIdNotDuplicated(null))
				.doesNotThrowAnyException();

			verify(userRepository, never()).findByStudentId(anyString());
		}
	}
}
