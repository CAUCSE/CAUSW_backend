package net.causw.app.main.domain.community.ceremony.service;

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

	public Page<CeremonyAdminListResult> getCeremonyList(CeremonyAdminListCondition condition, Pageable pageable) {
		Page<Ceremony> ceremonies = ceremonyReader.findAllForAdmin(
			condition.fromDate(), condition.toDate(), condition.state(), pageable);
		return ceremonies.map(ceremonyMapper::toAdminListResult);
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

		// TODO: 푸시알림 전송 (v2 알림 서비스 완성 후 구현)
	}

	@Transactional
	public void reject(String ceremonyId, String rejectReason) {
		Ceremony ceremony = ceremonyReader.findById(ceremonyId)
			.orElseThrow(CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);

		ceremonyValidator.validateAwaiting(ceremony);
		ceremonyWriter.reject(ceremony, rejectReason);
	}
}
