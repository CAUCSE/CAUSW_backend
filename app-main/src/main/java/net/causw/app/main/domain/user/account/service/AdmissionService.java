package net.causw.app.main.domain.user.account.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.implementation.FileWriter;
import net.causw.app.main.domain.notification.notification.event.AdmissionRequestedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.service.dto.request.AdmissionCreateCommand;
import net.causw.app.main.domain.user.account.service.dto.response.AdmissionResult;
import net.causw.app.main.domain.user.account.service.dto.response.AdmissionStateResult;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionReader;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionValidator;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdmissionService {

	private final AdmissionReader admissionReader;
	private final AdmissionValidator admissionValidator;
	private final AdmissionWriter admissionWriter;
	private final UserWriter userWriter;
	private final FileWriter fileWriter;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 재학정보 인증 신청을 생성합니다. (v3 presigned URL)
	 *
	 * 검증 사항:
	 * - 추방된 학번인지 검증
	 * - 사용자 상태가 AWAIT 또는 REJECT인 경우만 신청 가능
	 * - 기존 신청이 존재하지 않아야 함
	 * - 첨부 이미지 UUID 1개 이상 필수
	 * - 요청 학번이 다른 ACTIVE/INACTIVE/DROP 사용자와 중복되지 않아야 함
	 */
	@Transactional
	public AdmissionResult createAdmission(
		User user,
		AdmissionCreateCommand dto) {
		admissionValidator.validateAdmissionCreateWithUuids(user, dto.requestedStudentId(),
			dto.requestedAcademicStatus(), dto.graduationYear(), dto.attachImageUuids());

		List<UuidFile> uuidFiles = fileWriter.confirmFiles(dto.attachImageUuids(), FilePath.USER_ADMISSION);

		userWriter.updateStateToAwait(user);

		UserAdmission admission = admissionWriter.create(
			user,
			uuidFiles,
			dto.description(),
			dto.requestedAcademicStatus(),
			dto.requestedStudentId(),
			dto.requestedAdmissionYear(),
			dto.requestedDepartment(),
			dto.graduationYear());

		eventPublisher.publishEvent(new AdmissionRequestedEvent(user.getId(), admission.getRequestedAcademicStatus(),
			dto.requestedStudentId()));

		return AdmissionResult.from(admission);
	}

	/**
	 * 재학정보 인증 신청을 생성합니다. (v2 multipart)
	 *
	 * 검증 사항:
	 * - 추방된 학번인지 검증
	 * - 사용자 상태가 AWAIT 또는 REJECT인 경우만 신청 가능
	 * - 기존 신청이 존재하지 않아야 함
	 * - 첨부 이미지 1개 이상 필수
	 * - 요청 학번이 다른 ACTIVE/INACTIVE/DROP 사용자와 중복되지 않아야 함
	 */
	@Transactional
	public AdmissionResult createAdmissionV2(
		User user,
		AdmissionCreateCommand dto,
		List<MultipartFile> attachImages) {
		admissionValidator.validateAdmissionCreate(user, dto.requestedStudentId(),
			dto.requestedAcademicStatus(), dto.graduationYear(), attachImages);

		List<UuidFile> uuidFiles = fileWriter.uploadAndSaveList(attachImages, FilePath.USER_ADMISSION);

		userWriter.updateStateToAwait(user);

		UserAdmission admission = admissionWriter.create(
			user,
			uuidFiles,
			dto.description(),
			dto.requestedAcademicStatus(),
			dto.requestedStudentId(),
			dto.requestedAdmissionYear(),
			dto.requestedDepartment(),
			dto.graduationYear());

		eventPublisher.publishEvent(new AdmissionRequestedEvent(user.getId(), admission.getRequestedAcademicStatus(),
			dto.requestedStudentId()));

		return AdmissionResult.from(admission);
	}

	/**
	 * 사용자의 인증 신청 상태를 조회합니다.
	 */
	@Transactional(readOnly = true)
	public AdmissionStateResult getAdmissionState(User user) {
		boolean hasAdmission = admissionReader.existsByUserId(user.getId());
		return AdmissionStateResult.of(user, hasAdmission);
	}
}
