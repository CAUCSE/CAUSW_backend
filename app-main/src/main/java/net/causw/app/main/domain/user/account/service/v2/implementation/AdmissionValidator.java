package net.causw.app.main.domain.user.account.service.v2.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdmissionValidator {

	private final AdmissionReader admissionReader;

	/**
	 * 재학정보 인증 신청이 가능한 상태인지 검증합니다.
	 *
	 * - 사용자 상태가 AWAIT 또는 REJECT인 경우만 신청 가능
	 * - 기존 신청이 존재하지 않아야 함
	 * - 첨부 이미지 1개 이상 필수
	 */
	public void validateAdmissionCreate(User user, List<MultipartFile> attachImages) {
		validateUserStateForAdmission(user);
		validateNoDuplicateAdmission(user);
		validateAttachImages(attachImages);
	}

	/**
	 * 사용자 상태가 인증 신청 가능한 상태인지 검증합니다.
	 */
	public void validateUserStateForAdmission(User user) {
		if (!(user.getState() == UserState.AWAIT || user.getState() == UserState.REJECT)) {
			throw UserErrorCode.INVALID_USER_STATE_FOR_ADMISSION.toBaseException();
		}
	}

	/**
	 * 기존 신청이 존재하지 않는지 검증합니다.
	 */
	public void validateNoDuplicateAdmission(User user) {
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
}
