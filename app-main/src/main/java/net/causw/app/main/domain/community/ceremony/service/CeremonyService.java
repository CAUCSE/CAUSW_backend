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
import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyCreator;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.community.ceremony.util.CeremonyCreateMapper;
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

	@Transactional
	public CeremonyDetailResponseDto createCeremony(
		User user,
		@Valid CreateCeremonyRequestDto createCeremonyRequestDTO,
		List<MultipartFile> imageFileList) {

		// 경조사 상세 분류 직접 입력 검증
		if (createCeremonyRequestDTO.getCeremonyCategory() == CeremonyCategory.ETC) {
			if (createCeremonyRequestDTO.getCeremonyCustomCategory() == null
				|| createCeremonyRequestDTO.getCeremonyCustomCategory().isEmpty()) {
				throw CeremonyErrorCode.CUSTOM_CATEGORY_REQUIRED.toBaseException();
			}
		}

		// 관계 - 상세 관계 검증
		switch (createCeremonyRequestDTO.getRelationType()) {
			case FAMILY -> {
				if (createCeremonyRequestDTO.getFamilyRelation() == null) {
					throw CeremonyErrorCode.FAMILY_RELATION_REQUIRED.toBaseException();
				}
			}
			case ALUMNI -> {
				if (createCeremonyRequestDTO.getAlumniRelation() == null) {
					throw CeremonyErrorCode.ALUMNI_RELATION_REQUIRED.toBaseException();
				}
				if (createCeremonyRequestDTO.getAlumniName() == null) {
					throw CeremonyErrorCode.ALUMNI_NAME_REQUIRED.toBaseException();
				}
				if (createCeremonyRequestDTO.getAlumniAdmissionYear() == null) {
					throw CeremonyErrorCode.ALUMNI_ADMISSION_YEAR_REQUIRED.toBaseException();
				}
			}
		}

		// 경조사 종료 시간 설정 시 종료 날짜 또는 시작 시간 입력됐는지 검증
		if (createCeremonyRequestDTO.getEndTime() != null) {
			if (createCeremonyRequestDTO.getEndDate() == null) {
				throw CeremonyErrorCode.END_DATE_REQUIRED.toBaseException();
			}
			if (createCeremonyRequestDTO.getStartTime() == null) {
				throw CeremonyErrorCode.START_TIME_REQUIRED.toBaseException();
			}
		}

		// 전체 알림 전송이 false인 경우, 대상 학번이 입력되었는지 검증
		if (!createCeremonyRequestDTO.getIsSetAll()) {
			if (createCeremonyRequestDTO.getTargetAdmissionYears() == null
				|| createCeremonyRequestDTO.getTargetAdmissionYears().isEmpty()) {
				throw CeremonyErrorCode.TARGET_ADMISSION_YEARS_REQUIRED.toBaseException();
			}

			// 알림 대상 학번 검증
			for (String admissionYear : createCeremonyRequestDTO.getTargetAdmissionYears()) {
				if (!admissionYear.matches("^[0-9]{2}$")) {
					throw CeremonyErrorCode.INVALID_ADMISSION_YEARS_FORMAT.toBaseException();
				}
			}
		}

		// 전체 알림 전송이 true인 경우, 대상 학번은 빈 리스트(null)로 설정
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
