package net.causw.app.main.domain.user.account.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionCreateCommand;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionResult;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionStateResult;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionValidator;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdmissionService {

	private final AdmissionReader admissionReader;
	private final AdmissionValidator admissionValidator;
	private final AdmissionWriter admissionWriter;
	private final UserWriter userWriter;
	private final FileWriter fileWriter;

	/**
	 * v2 재학정보 인증 신청을 생성합니다.
	 *
	 * 검증 사항:
	 * - 사용자 상태가 AWAIT 또는 REJECT인 경우만 신청 가능
	 * - 기존 신청이 존재하지 않아야 함
	 * - 첨부 이미지 1개 이상 필수
	 */
	@Transactional
	public AdmissionResult createAdmission(
		User user,
		AdmissionCreateCommand dto,
		List<MultipartFile> attachImages) {

		// 인증 신청 생성 검증
		admissionValidator.validateAdmissionCreate(user, attachImages);

		// 이미지 파일 업로드
		List<UuidFile> uuidFiles = fileWriter.uploadAndSaveList(attachImages, FilePath.USER_ADMISSION);

		// 사용자 상태를 AWAIT으로 설정 (REJECT에서 재신청 시)
		userWriter.updateStateToAwait(user);

		// UserAdmission 생성 (v2 방식)
		UserAdmission admission = admissionWriter.create(
			user,
			uuidFiles,
			dto.description(),
			dto.requestedAcademicStatus(),
			dto.requestedStudentId(),
			dto.requestedAdmissionYear(),
			dto.requestedDepartment());

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
