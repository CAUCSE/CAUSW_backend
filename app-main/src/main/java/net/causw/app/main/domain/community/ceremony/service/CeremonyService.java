package net.causw.app.main.domain.community.ceremony.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.service.dto.request.CeremonyCreateCommand;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonyDetailResult;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonySummaryResult;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyCreator;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.community.ceremony.service.mapper.CeremonyCreateMapper;
import net.causw.app.main.domain.community.ceremony.service.mapper.CeremonyMapper;
import net.causw.app.main.domain.community.ceremony.util.CeremonyValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class CeremonyService {
	private final UuidFileService uuidFileService;
	private final CeremonyCreator ceremonyCreator;
	private final CeremonyReader ceremonyReader;
	private final CeremonyCreateMapper ceremonyCreateMapper;
	private final CeremonyMapper ceremonyMapper;
	private final CeremonyValidator ceremonyValidator;
	private final PageableFactory pageableFactory;

	@Transactional
	public CeremonyDetailResult createCeremony(
		User user,
		@Valid CeremonyCreateCommand command,
		List<MultipartFile> imageFileList) {
		ceremonyValidator.validateForCreate(command);

		List<String> targetAdmissionYears = command.isSetAll()
			? new ArrayList<>()
			: command.targetAdmissionYears();

		List<UuidFile> uuidFileList = (imageFileList == null || imageFileList.isEmpty())
			? List.of()
			: uuidFileService.saveFileList(imageFileList, FilePath.CEREMONY);

		Ceremony ceremony = ceremonyCreateMapper.toCeremony(user, command, targetAdmissionYears,
			uuidFileList);
		ceremonyCreator.save(ceremony);
		return ceremonyMapper.toDetailResult(ceremony);
	}

	@Transactional(readOnly = true)
	public CeremonyDetailResult getCeremony(String ceremonyId, CeremonyContext context, User user) {
		Ceremony ceremony = ceremonyReader.findById(ceremonyId).orElseThrow(
			CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);

		if (context == CeremonyContext.MY) {
			if (!ceremony.getUser().getId().equals(user.getId())) {
				throw CeremonyErrorCode.ACCESS_ONLY_APPLICANT.toBaseException();
			}
			return ceremonyMapper.toMyDetailResult(ceremony);
		}

		if (ceremony.getCeremonyState() != CeremonyState.ACCEPT) {
			throw CeremonyErrorCode.CEREMONY_NOT_FOUND.toBaseException();
		}
		return ceremonyMapper.toDetailResult(ceremony);
	}

	@Transactional(readOnly = true)
	public Page<CeremonySummaryResult> getOngoingCeremonyPage(String type, Integer pageNum) {
		Page<Ceremony> ceremonies;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE);

		ceremonies = ceremonyReader.findOngoingOrderByStartedAtDesc(type, LocalDate.now(),
			LocalTime.now(), pageable);

		return ceremonies.map(ceremonyMapper::toSummaryResult);
	}

	@Transactional(readOnly = true)
	public Page<CeremonySummaryResult> getUpcomingCeremonyPage(String type, Integer pageNum) {
		Page<Ceremony> ceremonies;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE);

		ceremonies = ceremonyReader.findUpcomingOrderByStartedAtAsc(type, LocalDate.now(),
			LocalTime.now(), pageable);

		return ceremonies.map(ceremonyMapper::toSummaryResult);
	}

	@Transactional(readOnly = true)
	public Page<CeremonySummaryResult> getPastCeremonyPage(String type, Integer pageNum) {
		Page<Ceremony> ceremonies;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE);

		ceremonies = ceremonyReader.findPastOrderByStartedAtDesc(type, LocalDate.now(),
			LocalTime.now(), pageable);

		return ceremonies.map(ceremonyMapper::toSummaryResult);
	}

	@Transactional(readOnly = true)
	public Page<CeremonySummaryResult> getMyCeremonyPage(String userId, CeremonyState state, Integer pageNum) {
		Page<Ceremony> ceremonies;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE);
		if (state == CeremonyState.CLOSE) {
			throw CeremonyErrorCode.INVALID_CEREMONY_STATE.toBaseException();
		}
		ceremonies = ceremonyReader.findByUserIdAndCeremonyStateOrderByStartedAtDesc(userId, state, pageable);
		return ceremonies.map(ceremonyMapper::toMySummaryResult);
	}
}
