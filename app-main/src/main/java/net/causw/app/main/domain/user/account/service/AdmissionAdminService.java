package net.causw.app.main.domain.user.account.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.service.AdmissionNotificationService;
import net.causw.app.main.domain.user.academic.event.CertifiedUserCreatedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionListCondition;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionResult;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionLogWriter;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionReader;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionValidator;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionWriter;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdmissionAdminService {

	private final AdmissionReader admissionReader;
	private final AdmissionValidator admissionValidator;
	private final AdmissionWriter admissionWriter;
	private final AdmissionLogWriter admissionLogWriter;
	private final UserWriter userWriter;
	private final ApplicationEventPublisher eventPublisher;
	private final AdmissionNotificationService admissionNotificationService;

	/**
	 * 재학인증 신청 목록을 조회합니다.
	 */
	@Transactional(readOnly = true)
	public Page<AdmissionResult> getAdmissionList(
		AdmissionListCondition condition,
		Pageable pageable) {
		return admissionReader
			.findAdmissionList(condition.keyword(), condition.userState(), pageable)
			.map(AdmissionResult::from);
	}

	/**
	 * 재학인증 신청 상세를 조회합니다.
	 */
	@Transactional(readOnly = true)
	public AdmissionResult getAdmissionDetail(String admissionId) {
		return AdmissionResult.from(admissionReader.findAdmissionDetail(admissionId));
	}

	/**
	 * 재학인증 신청을 승인합니다.
	 *
	 * 하나의 트랜잭션 안에서 다음을 수행합니다:
	 * 1. 승인 전 중복 체크 (이메일/닉네임/전화번호/학번)
	 * 2. UserAdmission.requested 필드 기반으로 User 정보 업데이트
	 * 3. User.userState = ACTIVE, role = COMMON, academicStatus 확정
	 * 4. CertifiedUserCreatedEvent 발행
	 * 5. UserAdmissionLog 생성
	 * 6. 기존 UserAdmission 삭제
	 */
	@Transactional
	public void approveAdmission(String admissionId, User adminUser) {
		// User + AttachImageList 모두 fetch join — 검증·업데이트·로그 생성에서 전부 사용됨
		UserAdmission admission = admissionReader.findAdmissionDetail(admissionId);

		// 승인 전 학번 중복 체크
		admissionValidator.validateStudentIdNotDuplicated(admission.getRequestedStudentId());

		// 유저 정보 업데이트 + 상태 변경 + 역할 변경 + 학적 상태 확정
		User targetUser = admission.getUser();
		userWriter.approveAdmission(targetUser, admission);

		// 승인 시 CertifiedUserCreatedEvent 발행
		eventPublisher.publishEvent(new CertifiedUserCreatedEvent(targetUser.getId()));

		// 로그 생성
		admissionLogWriter.createAcceptLog(admission, adminUser);

		// 신청 삭제
		admissionWriter.delete(admission);

		admissionNotificationService.sendApprovedAdmissionToUser(targetUser.getId(), adminUser.getId());
	}

	/**
	 * 재학인증 신청을 거절합니다.
	 *
	 * 하나의 트랜잭션 안에서 다음을 수행합니다:
	 * 1. User.userState = REJECT
	 * 2. User.rejectOrDropReason 기록
	 * 3. UserAdmissionLog 생성
	 * 4. 기존 UserAdmission 삭제
	 */
	@Transactional
	public void rejectAdmission(String admissionId, User adminUser, String rejectReason) {
		if (rejectReason == null || rejectReason.isBlank()) {
			throw UserErrorCode.ADMISSION_REJECT_REASON_REQUIRED.toBaseException();
		}

		// User + AttachImageList 모두 fetch join — 로그 생성에서 전부 사용됨
		UserAdmission admission = admissionReader.findAdmissionDetail(admissionId);
		User targetUser = admission.getUser();

		// 유저 상태 REJECT + 거절 사유 기록
		userWriter.rejectAdmission(targetUser, rejectReason);

		// 로그 생성
		admissionLogWriter.createRejectLog(admission, adminUser, rejectReason);

		// 신청 삭제
		admissionWriter.delete(admission);

		admissionNotificationService.sendRejectedAdmissionToUser(targetUser.getId(), adminUser.getId());
	}
}
