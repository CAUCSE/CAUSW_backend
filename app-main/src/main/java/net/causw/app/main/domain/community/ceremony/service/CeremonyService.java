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
import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyDetailResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonySummaryResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v2.mapper.CeremonyCreateMapper;
import net.causw.app.main.domain.community.ceremony.api.v2.mapper.CeremonyDtoMapper;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyCreator;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.community.ceremony.util.CeremonyTypeParser;
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
	private final CeremonyDtoMapper ceremonyDtoMapper;
	private final CeremonyTypeParser ceremonyTypeParser;
	private final CeremonyValidator ceremonyValidator;
	private final PageableFactory pageableFactory;

	@Transactional
	public CeremonyDetailResponseDto createCeremony(
		User user,
		@Valid CreateCeremonyRequestDto createCeremonyRequestDTO,
		List<MultipartFile> imageFileList) {
		ceremonyValidator.validateForCreate(createCeremonyRequestDTO);

		List<String> targetAdmissionYears = createCeremonyRequestDTO.getIsSetAll()
			? new ArrayList<>()
			: createCeremonyRequestDTO.getTargetAdmissionYears();

		List<UuidFile> uuidFileList = (imageFileList == null || imageFileList.isEmpty())
			? List.of()
			: uuidFileService.saveFileList(imageFileList, FilePath.CEREMONY);

		Ceremony ceremony = ceremonyCreateMapper.fromRequest(user, createCeremonyRequestDTO, targetAdmissionYears,
			uuidFileList);
		ceremonyCreator.save(ceremony);
		return CeremonyDtoMapper.INSTANCE.toCeremonyDetailResponseDto(ceremony);
	}

	@Transactional(readOnly = true)
	public CeremonyDetailResponseDto getCeremony(String ceremonyId, CeremonyContext context, User user) {
		Ceremony ceremony = ceremonyReader.findById(ceremonyId).orElseThrow(
			CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);

		if (context == CeremonyContext.MY) {
			if (!ceremony.getUser().getId().equals(user.getId())) {
				throw CeremonyErrorCode.ACCESS_ONLY_APPLICANT.toBaseException();
			}
			return CeremonyDtoMapper.INSTANCE.toMyCeremonyDetailResponseDto(ceremony);
		}

		if (ceremony.getCeremonyState() != CeremonyState.ACCEPT) {
			throw CeremonyErrorCode.CEREMONY_NOT_FOUND.toBaseException();
		}
		return CeremonyDtoMapper.INSTANCE.toCeremonyDetailResponseDto(ceremony);
	}

	@Transactional(readOnly = true)
	public Page<CeremonySummaryResponseDto> getOngoingCeremonyPage(String type, Integer pageNum) {
		Page<Ceremony> ceremonies;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE);
		CeremonyType ceremonyType;

		if (ceremonyTypeParser.parseTypeOrNull(type) == null) {
			ceremonies = ceremonyReader.findAllOngoingOrderByStartedAtDesc(LocalDate.now(), LocalTime.now(), pageable);
		} else {
			ceremonyType = CeremonyType.fromString(type);
			ceremonies = ceremonyReader.findOngoingByTypeOrderByStartedAtDesc(ceremonyType, LocalDate.now(),
				LocalTime.now(), pageable);
		}

		return ceremonies.map(ceremonyDtoMapper::toCeremonySummaryResponseDto);
	}

	@Transactional(readOnly = true)
	public Page<CeremonySummaryResponseDto> getUpcomingCeremonyPage(String type, Integer pageNum) {
		Page<Ceremony> ceremonies;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE);
		CeremonyType ceremonyType;

		if (ceremonyTypeParser.parseTypeOrNull(type) == null) {
			ceremonies = ceremonyReader.findAllUpcomingOrderByStartedAtAsc(LocalDate.now(), LocalTime.now(), pageable);
		} else {
			ceremonyType = CeremonyType.fromString(type);
			ceremonies = ceremonyReader.findUpcomingByTypeOrderByStartedAtAsc(ceremonyType, LocalDate.now(),
				LocalTime.now(), pageable);
		}

		return ceremonies.map(ceremonyDtoMapper::toCeremonySummaryResponseDto);
	}

	@Transactional(readOnly = true)
	public Page<CeremonySummaryResponseDto> getPastCeremonyPage(String type, Integer pageNum) {
		Page<Ceremony> ceremonies;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE);
		CeremonyType ceremonyType;

		if (ceremonyTypeParser.parseTypeOrNull(type) == null) {
			ceremonies = ceremonyReader.findAllPastOrderByStartedAtDesc(LocalDate.now(), LocalTime.now(), pageable);
		} else {
			ceremonyType = CeremonyType.fromString(type);
			ceremonies = ceremonyReader.findPastByTypeOrderByStartedAtDesc(ceremonyType, LocalDate.now(),
				LocalTime.now(), pageable);
		}

		return ceremonies.map(ceremonyDtoMapper::toCeremonySummaryResponseDto);
	}

	@Transactional(readOnly = true)
	public Page<CeremonySummaryResponseDto> getMyCeremonyPage(String userId, CeremonyState state, Integer pageNum) {
		Page<Ceremony> ceremonies;
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE);
		if (state == CeremonyState.CLOSE) {
			throw CeremonyErrorCode.CEREMONY_NOT_FOUND.toBaseException();
		}
		ceremonies = ceremonyReader.findMyByStateOrderByStartedAtDesc(userId, state, pageable);
		return ceremonies.map(ceremonyDtoMapper::toMyCeremonySummaryResponseDto);
	}
}
