package net.causw.app.main.domain.community.ceremony.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyDetailResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v2.mapper.CeremonyDtoMapper;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyCreator;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.community.ceremony.api.v2.mapper.CeremonyCreateMapper;
import net.causw.app.main.domain.community.ceremony.validation.CeremonyValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

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
	private final CeremonyValidator ceremonyValidator;

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
		return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
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
		return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
	}
}
