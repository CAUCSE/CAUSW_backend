package net.causw.app.main.domain.community.ceremony.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.service.dto.request.CeremonyAdminListCondition;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonyAdminListResult;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonyDetailResult;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyWriter;
import net.causw.app.main.domain.community.ceremony.service.mapper.CeremonyMapper;
import net.causw.app.main.domain.community.ceremony.util.CeremonyValidator;
import net.causw.app.main.domain.notification.notification.event.CeremonyApprovedEvent;
import net.causw.app.main.domain.notification.notification.event.CeremonyNotificationEvent;
import net.causw.app.main.domain.notification.notification.event.CeremonyRejectedEvent;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CeremonyAdminService {

	private final CeremonyReader ceremonyReader;
	private final CeremonyWriter ceremonyWriter;
	private final CeremonyValidator ceremonyValidator;
	private final CeremonyMapper ceremonyMapper;
	private final ApplicationEventPublisher eventPublisher;

	public Page<CeremonyAdminListResult> getCeremonyList(CeremonyAdminListCondition condition, Pageable pageable) {
		Page<Ceremony> ceremonies = ceremonyReader.findAllForAdmin(
			condition.fromDate(), condition.toDate(), condition.state(), pageable);
		return ceremonies.map(ceremonyMapper::toAdminListResult);
	}

	public long getPendingCount() {
		return ceremonyReader.countAwaitCeremony();
	}

	public CeremonyDetailResult getCeremonyDetail(String ceremonyId) {
		Ceremony ceremony = ceremonyReader.findById(ceremonyId)
			.orElseThrow(CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);
		return ceremonyMapper.toAdminDetailResult(ceremony);
	}

	@Transactional
	public void approve(String ceremonyId) {
		Ceremony ceremony = ceremonyReader.findById(ceremonyId)
			.orElseThrow(CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);

		ceremonyValidator.validateAwaiting(ceremony);
		ceremonyWriter.approve(ceremony);

		// 대상 학번 유저들에게 경조사 공지 알림
		eventPublisher.publishEvent(new CeremonyNotificationEvent(ceremonyId));
		// 신청자에게 승인 결과 알림
		eventPublisher.publishEvent(new CeremonyApprovedEvent(ceremonyId));
	}

	@Transactional
	public void reject(String ceremonyId, String rejectReason) {
		Ceremony ceremony = ceremonyReader.findById(ceremonyId)
			.orElseThrow(CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);

		ceremonyValidator.validateAwaiting(ceremony);
		ceremonyWriter.reject(ceremony, rejectReason);

		// 신청자에게 거절 결과 알림
		eventPublisher.publishEvent(new CeremonyRejectedEvent(ceremonyId, rejectReason));
	}
}
