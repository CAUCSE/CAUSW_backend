package net.causw.app.main.domain.user.account.service.v2.implementation;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserAdmissionRepository;
import net.causw.app.main.domain.user.account.repository.user.query.UserAdmissionQueryRepository;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdmissionReader {

	private final UserAdmissionRepository admissionRepository;
	private final UserAdmissionQueryRepository admissionQueryRepository;

	/**
	 * 사용자 ID로 현재 UserAdmission을 조회합니다.
	 */
	public Optional<UserAdmission> findByUserId(String userId) {
		return admissionRepository.findByUser_Id(userId);
	}

	/**
	 * 사용자에게 이미 신청이 존재하는지 확인합니다.
	 */
	public boolean existsByUserId(String userId) {
		return admissionRepository.existsByUser_Id(userId);
	}

	/**
	 * 관리자용: 재학인증 신청 목록을 페이징 조회합니다.
	 */
	public Page<UserAdmission> findAdmissionList(
		String keyword,
		UserState userState,
		Pageable pageable) {
		return admissionQueryRepository.findAdmissionList(keyword, userState, pageable);
	}

	/**
	 * 관리자용: 재학인증 신청 상세를 조회합니다. (첨부 이미지 포함)
	 */
	public UserAdmission findAdmissionDetail(String admissionId) {
		return admissionQueryRepository.findByIdWithDetails(admissionId)
			.orElseThrow(UserErrorCode.ADMISSION_NOT_FOUND::toBaseException);
	}
}
