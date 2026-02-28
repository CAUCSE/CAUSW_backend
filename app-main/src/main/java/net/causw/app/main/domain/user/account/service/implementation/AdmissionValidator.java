package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdmissionValidator {

	private final AdmissionReader admissionReader;
	private final UserRepository userRepository;

	/**
	 * 재학정보 인증 신청이 가능한 상태인지 검증합니다.
	 *
	 * - 사용자 상태가 AWAIT 또는 REJECT인 경우만 신청 가능
	 * - 기존 신청이 존재하지 않아야 함
	 * - 첨부 이미지 1개 이상 필수
	 * - 요청 학번이 다른 ACTIVE/INACTIVE/DROP 사용자와 중복되지 않아야 함
	 */
	public void validateAdmissionCreate(User user, String requestedStudentId,
		AcademicStatus requestedAcademicStatus, Integer graduationYear,
		List<MultipartFile> attachImages) {
		validateUserStateForAdmission(user);
		validateNoExistingAdmission(user);
		validateAttachImages(attachImages);
		validateStudentIdNotDuplicated(requestedStudentId);
		validateGraduationYear(requestedAcademicStatus, graduationYear);
	}

	/**
	 * 사용자 상태가 인증 신청 가능한 상태인지 검증합니다.
	 */
	public void validateUserStateForAdmission(User user) {
		if (!user.canApplyAdmission()) {
			throw UserErrorCode.INVALID_USER_STATE_FOR_ADMISSION.toBaseException();
		}
	}

	/**
	 * 기존 신청이 존재하지 않는지 검증합니다.
	 */
	public void validateNoExistingAdmission(User user) {
		if (admissionReader.existsByUserId(user.getId())) {
			throw UserErrorCode.ADMISSION_ALREADY_EXISTS.toBaseException();
		}
	}

	/**
	 * 첨부 이미지가 1개 이상 존재하는지 검증합니다.
	 */
	public void validateAttachImages(List<MultipartFile> attachImages) {
		if (attachImages == null || attachImages.isEmpty()) {
			throw UserErrorCode.ADMISSION_IMAGE_REQUIRED.toBaseException();
		}
	}

	/**
	 * 졸업자(GRADUATED)인 경우 졸업연도가 필수인지 검증합니다.
	 */
	public void validateGraduationYear(AcademicStatus requestedAcademicStatus, Integer graduationYear) {
		if (requestedAcademicStatus == AcademicStatus.GRADUATED && graduationYear == null) {
			throw UserErrorCode.GRADUATION_YEAR_REQUIRED.toBaseException();
		}
	}

	/**
	 * 요청 학번이 이미 ACTIVE/INACTIVE 사용자에게 할당되어 있거나,
	 * DROP 상태의 사용자가 사용 중이면 예외를 발생시킵니다.
	 */
	public void validateStudentIdNotDuplicated(String requestedStudentId) {
		if (requestedStudentId == null) {
			return;
		}

		userRepository.findByStudentId(requestedStudentId).ifPresent(existingUser -> {
			UserState state = existingUser.getState();
			if (state == UserState.ACTIVE || state == UserState.INACTIVE || state == UserState.DROP) {
				throw UserErrorCode.STUDENT_ID_ALREADY_EXIST.toBaseException();
			}
		});
	}
}
