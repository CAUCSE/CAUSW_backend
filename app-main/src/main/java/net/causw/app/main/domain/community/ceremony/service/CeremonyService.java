package net.causw.app.main.domain.community.ceremony.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.community.ceremony.api.v1.dto.CeremonyDetailResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v1.dto.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.api.v1.mapper.CeremonyDtoMapper;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.repository.CeremonyRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CeremonyService {
	private final UuidFileService uuidFileService;
	private final CeremonyRepository ceremonyRepository;

	@Transactional
	public CeremonyDetailResponseDto createCeremony(
		User user,
		@Valid CreateCeremonyRequestDto createCeremonyRequestDTO,
		List<MultipartFile> imageFileList) {

		// 전체 알림 전송이 false인 경우, 대상 학번이 입력되었는지 검증
		if (!createCeremonyRequestDTO.getIsSetAll()) {
			if (createCeremonyRequestDTO.getTargetAdmissionYears() == null
				|| createCeremonyRequestDTO.getTargetAdmissionYears().isEmpty()) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					MessageUtil.CEREMONY_TARGET_ADMISSION_YEARS_REQUIRED);
			}

			// 학번 형식 (숫자 2자리) 검증
			for (String admissionYear : createCeremonyRequestDTO.getTargetAdmissionYears()) {
				if (!admissionYear.matches("^[0-9]{2}$")) {
					throw new BadRequestException(
						ErrorCode.INVALID_PARAMETER,
						MessageUtil.CEREMONY_INVALID_ADMISSION_YEAR_FORMAT);
				}
			}
		}

		// 전체 알림 전송이 true인 경우, 대상 학번은 빈 리스트(null)로 설정
		List<String> targetAdmissionYears = createCeremonyRequestDTO.getIsSetAll()
			? new ArrayList<>()
			: createCeremonyRequestDTO.getTargetAdmissionYears();

		List<UuidFile> uuidFileList = (imageFileList == null || imageFileList.isEmpty())
			? List.of()
			: uuidFileService.saveFileList(imageFileList, FilePath.USER_ACADEMIC_RECORD_APPLICATION);

		// 관계 - 상세 관계 검증
		switch (createCeremonyRequestDTO.getRelationType()) {
			case FAMILY -> {
				if (createCeremonyRequestDTO.getFamilyRelation() == null) {
					throw new BadRequestException(
						ErrorCode.INVALID_USER_DATA_REQUEST,
						MessageUtil.CEREMONY_FAMILY_RELATION_REQUIRED);
				}
			}
			case ALUMNI -> {
				if (createCeremonyRequestDTO.getAlumniRelation() == null) {
					throw new BadRequestException(
						ErrorCode.INVALID_USER_DATA_REQUEST,
						MessageUtil.CEREMONY_ALUMNI_NAME_REQUIRED);
				}
				if (createCeremonyRequestDTO.getAlumniAdmissionYear() == null) {
					throw new BadRequestException(
						ErrorCode.INVALID_USER_DATA_REQUEST,
						MessageUtil.CEREMONY_ALUMNI_ADMISSION_YEAR_REQUIRED);
				}
			}
		}

		if (createCeremonyRequestDTO.getEndDate() == null && createCeremonyRequestDTO.getEndTime() != null) {
			throw new BadRequestException(
				ErrorCode.INVALID_USER_DATA_REQUEST,
				MessageUtil.CEREMONY_ENDDATE_REQUIRED);
		}

		Ceremony ceremony = Ceremony.createWithImages(
			user,
			createCeremonyRequestDTO.getCeremonyType(),
			createCeremonyRequestDTO.getCeremonyCategory(),
			createCeremonyRequestDTO.getStartDate(),
			createCeremonyRequestDTO.getEndDate(),
			createCeremonyRequestDTO.getStartTime(),
			createCeremonyRequestDTO.getEndTime(),
			createCeremonyRequestDTO.getRelationType(),
			createCeremonyRequestDTO.getFamilyRelation(),
			createCeremonyRequestDTO.getAlumniRelation(),
			createCeremonyRequestDTO.getAlumniName(),
			createCeremonyRequestDTO.getAlumniAdmissionYear(),
			createCeremonyRequestDTO.getContent(),
			createCeremonyRequestDTO.getAddress(),
			createCeremonyRequestDTO.getPostalAddress(),
			createCeremonyRequestDTO.getDetailedAddress(),
			createCeremonyRequestDTO.getContact(),
			createCeremonyRequestDTO.getLink(),
			createCeremonyRequestDTO.getIsSetAll(),
			targetAdmissionYears,
			uuidFileList);
		ceremonyRepository.save(ceremony);

		return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
	}

	@Transactional(readOnly = true)
	public CeremonyDetailResponseDto getCeremony(String ceremonyId, CeremonyContext context, User user) {
		Ceremony ceremony = ceremonyRepository.findById(ceremonyId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.CEREMONY_NOT_FOUND));

		if (context == CeremonyContext.MY) {
			if (!ceremony.getUser().getId().equals(user.getId())) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					MessageUtil.CEREMONY_ACCESS_MY_ONLY);
			}
			return CeremonyDtoMapper.INSTANCE.toMyCeremonyDetailResponseDto(ceremony);
		}
		return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
	}
}
