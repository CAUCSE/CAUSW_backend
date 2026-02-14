package net.causw.app.main.domain.user.account.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionListCondition;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionResult;
import net.causw.app.main.domain.user.account.service.v2.implementation.AdmissionReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdmissionAdminService {

	private final AdmissionReader admissionReader;

	/**
	 * 관리자용: 재학인증 신청 목록을 조회합니다.
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
	 * 관리자용: 재학인증 신청 상세를 조회합니다.
	 */
	@Transactional(readOnly = true)
	public AdmissionResult getAdmissionDetail(String admissionId) {
		return AdmissionResult.from(admissionReader.findAdmissionDetail(admissionId));
	}
}
