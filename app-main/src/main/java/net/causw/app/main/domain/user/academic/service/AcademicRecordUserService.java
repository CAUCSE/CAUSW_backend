package net.causw.app.main.domain.user.academic.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.academic.event.AcademicStatusChangeEvent;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationWriter;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordLogCreator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.exception.errorcode.AcademicRecordApplicationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademicRecordUserService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UuidFileService uuidFileService;
	private final AcademicRecordApplicationWriter applicationWriter;
	private final AcademicRecordLogCreator logCreator;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 졸업 상태 변경 요청을 처리한다.
	 *
	 * @param requester 요청 사용자
	 * @param request 졸업 변경 요청 정보
	 * @return 학적 상태 변경 결과
	 */
	@Transactional
	public AcademicStatusResponse<GraduationDetailsResponse> updateStatusToGraduated(
		User requester,
		GraduationApplicationRequest request) {
		User user = userReader.findUserById(requester.getId());
		validateGraduationTransition(user);

		AcademicStatus previousStatus = user.getAcademicStatus();

		user.setAcademicStatus(AcademicStatus.GRADUATED);
		user.setGraduationYear(request.graduationYear());
		userWriter.save(user);

		eventPublisher.publishEvent(new AcademicStatusChangeEvent(
			user.getId(),
			previousStatus,
			AcademicStatus.GRADUATED));

		UserAcademicRecordLog graduationLog = logCreator.createGraduationLog(
			user,
			request.graduationYear(),
			request.note());

		GraduationDetailsResponse details = new GraduationDetailsResponse(
			graduationLog.getId(),
			request.graduationYear(),
			graduationLog.getCreatedAt());

		return new AcademicStatusResponse<>(
			AcademicStatus.GRADUATED,
			user.getAcademicStatus(),
			details);
	}

	/**
	 * 재학 상태 변경 신청을 접수한다.
	 *
	 * @param requester 요청 사용자
	 * @param request 재학 변경 신청 정보
	 * @param imageFileList 재학 증빙 이미지 파일 목록
	 * @return 학적 상태 변경 신청 결과
	 */
	@Transactional
	public AcademicStatusResponse<EnrollmentDetailsResponse> updateStatusToEnrolled(
		User requester,
		EnrollmentApplicationRequest request,
		List<MultipartFile> imageFileList) {
		User user = userReader.findUserById(requester.getId());
		List<MultipartFile> uploadedImageFileList = extractUploadedImageFiles(imageFileList);
		validateEnrollmentTransition(user, uploadedImageFileList);

		List<UuidFile> savedFiles = uuidFileService.saveFileList(
			uploadedImageFileList,
			FilePath.USER_ACADEMIC_RECORD_APPLICATION);

		UserAcademicRecordApplication application = applicationWriter.createEnrollmentApplication(
			user,
			request.note(),
			savedFiles);

		EnrollmentDetailsResponse details = new EnrollmentDetailsResponse(
			application.getId(),
			application.getAcademicRecordRequestStatus(),
			application.getCreatedAt());

		return new AcademicStatusResponse<>(
			AcademicStatus.ENROLLED,
			user.getAcademicStatus(),
			details);
	}

	private void validateGraduationTransition(User user) {
		if (user.getAcademicStatus() != AcademicStatus.ENROLLED) {
			throw AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_INVALID_STATUS_TRANSITION.toBaseException();
		}
	}

	private void validateEnrollmentTransition(User user, List<MultipartFile> imageFileList) {
		if (user.getAcademicStatus() != AcademicStatus.GRADUATED) {
			throw AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_INVALID_STATUS_TRANSITION.toBaseException();
		}

		if (imageFileList.isEmpty()) {
			throw AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_ENROLLMENT_IMAGE_REQUIRED.toBaseException();
		}
	}

	private List<MultipartFile> extractUploadedImageFiles(List<MultipartFile> imageFileList) {
		if (imageFileList == null) {
			return List.of();
		}

		return imageFileList.stream()
			.filter(file -> file != null && !file.isEmpty())
			.toList();
	}
}
