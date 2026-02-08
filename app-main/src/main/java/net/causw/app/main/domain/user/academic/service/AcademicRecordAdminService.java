package net.causw.app.main.domain.user.academic.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.service.dto.request.AcademicRecordApplicationListCondition;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicRecordApplicationDetailResult;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicRecordApplicationSummaryResult;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationReader;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationWriter;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordLogCreator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademicRecordAdminService {

	private final AcademicRecordApplicationReader applicationReader;
	private final AcademicRecordApplicationWriter applicationWriter;
	private final AcademicRecordLogCreator logCreator;

	/**
	 * 학적 변경 신청 목록을 조건에 따라 페이징 조회한다.
	 */
	@Transactional(readOnly = true)
	public Page<AcademicRecordApplicationSummaryResult> getApplications(
		AcademicRecordApplicationListCondition condition) {
		return applicationReader.findApplications(condition)
			.map(AcademicRecordApplicationSummaryResult::from);
	}

	/**
	 * 학적 변경 신청 상세 정보를 조회한다.
	 */
	@Transactional(readOnly = true)
	public AcademicRecordApplicationDetailResult getApplicationDetail(String applicationId) {
		return AcademicRecordApplicationDetailResult.from(
			applicationReader.findByIdWithDetails(applicationId));
	}

	/**
	 * 학적 변경 신청을 승인하고 처리 로그를 기록한다.
	 */
	@Transactional
	public void approve(User admin, String applicationId) {
		UserAcademicRecordApplication application = applicationReader.findById(applicationId);
		applicationWriter.approve(application);
		logCreator.createFromApplication(admin, application);
	}

	/**
	 * 학적 변경 신청을 반려하고 처리 로그를 기록한다.
	 */
	@Transactional
	public void reject(User admin, String applicationId, String rejectReason) {
		UserAcademicRecordApplication application = applicationReader.findById(applicationId);
		applicationWriter.reject(application, rejectReason);
		logCreator.createFromApplication(admin, application);
	}
}
