package net.causw.app.main.domain.community.ceremony.service.v1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v1.UuidFileServiceV1;
import net.causw.app.main.domain.community.ceremony.api.v1.dto.CeremonyNotificationSettingResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v1.dto.CeremonyResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v1.dto.CreateCeremonyNotificationSettingDto;
import net.causw.app.main.domain.community.ceremony.api.v1.dto.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.api.v1.dto.UpdateCeremonyStateRequestDto;
import net.causw.app.main.domain.community.ceremony.api.v1.mapper.CeremonyDtoMapper;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.repository.CeremonyRepository;
import net.causw.app.main.domain.notification.notification.api.v1.dto.CeremonyListNotificationDto;
import net.causw.app.main.domain.notification.notification.api.v1.mapper.NotificationDtoMapper;
import net.causw.app.main.domain.notification.notification.entity.CeremonyNotificationSetting;
import net.causw.app.main.domain.notification.notification.repository.CeremonyNotificationSettingRepository;
import net.causw.app.main.domain.notification.notification.service.v1.CeremonyNotificationService;
import net.causw.app.main.domain.user.academic.event.CertifiedUserCreatedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CeremonyService {
	private final CeremonyRepository ceremonyRepository;
	private final UserRepository userRepository;
	private final CeremonyNotificationService ceremonyNotificationService;
	private final UuidFileServiceV1 uuidFileService;
	private final CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;
	private final PageableFactory pageableFactory;

	@Transactional
	public CeremonyResponseDto createCeremony(
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

		Ceremony ceremony = Ceremony.createWithImages(
			user,
			createCeremonyRequestDTO.getCategory(),
			createCeremonyRequestDTO.getDescription(),
			createCeremonyRequestDTO.getStartDate(),
			createCeremonyRequestDTO.getEndDate(),
			createCeremonyRequestDTO.getIsSetAll(),
			targetAdmissionYears,
			uuidFileList);
		ceremonyRepository.save(ceremony);

		return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
	}

	@Transactional(readOnly = true)
	public Page<CeremonyListNotificationDto> getUserCeremonyResponses(User user, CeremonyState state, Integer pageNum) {
		Page<Ceremony> ceremonies = ceremonyRepository.findAllByUserAndCeremonyStateOrderByCreatedAtDesc(user, state,
			pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE));

		return ceremonies.map(NotificationDtoMapper.INSTANCE::toCeremonyListNotificationDto);
	}

	@Transactional(readOnly = true)
	public Page<CeremonyListNotificationDto> getAllUserAwaitingCeremonyPage(Integer pageNum) {
		Page<Ceremony> ceremonies = ceremonyRepository.findByCeremonyStateOrderByCreatedAtDesc(CeremonyState.AWAIT,
			pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE));

		return ceremonies.map(NotificationDtoMapper.INSTANCE::toCeremonyListNotificationDto);
	}

	@Transactional(readOnly = true)
	public CeremonyResponseDto getCeremony(String ceremonyId, CeremonyContext context, User user) {
		Ceremony ceremony = ceremonyRepository.findById(ceremonyId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.CEREMONY_NOT_FOUND));

		// context에 따라 다르게 처리
		switch (context) {
			case MY: // 내 경조사 목록
				if (!ceremony.getUser().getId().equals(user.getId())) {
					throw new BadRequestException(
						ErrorCode.API_NOT_ACCESSIBLE,
						MessageUtil.CEREMONY_ACCESS_MY_ONLY);
				}
				return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
			case ADMIN: // 관리자용 경조사 관리 페이지
				if (!user.getRoles().contains(Role.ADMIN)) {
					throw new BadRequestException(
						ErrorCode.API_NOT_ACCESSIBLE,
						MessageUtil.CEREMONY_ACCESS_ADMIN_ONLY);
				}
				return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
			case GENERAL:
			default:
				return CeremonyDtoMapper.INSTANCE.toCeremonyResponseDto(ceremony);
		}
	}

	@Transactional
	public CeremonyResponseDto updateUserCeremonyStatus(UpdateCeremonyStateRequestDto updateDto) {
		Ceremony ceremony = ceremonyRepository.findById(updateDto.getCeremonyId()).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.CEREMONY_NOT_FOUND));
		ceremony.updateCeremonyState(updateDto.getTargetCeremonyState());

		if (updateDto.getTargetCeremonyState() == CeremonyState.ACCEPT) {
			Integer writerAdmissionYear = ceremony.getUser().getAdmissionYear();

			ceremonyNotificationService.sendByAdmissionYear(writerAdmissionYear, updateDto.getCeremonyId());
		} else { // state가 reject, await, close로 바뀌는 경우 (close는 별도 처리)
			ceremony.updateNote(updateDto.getRejectMessage());
			return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
		}

		ceremonyRepository.save(ceremony);

		return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
	}

	@Transactional
	public CeremonyResponseDto closeUserCeremonyStatus(User user, String ceremonyId) {
		Ceremony ceremony = ceremonyRepository.findByIdAndUser(ceremonyId, user).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.CEREMONY_NOT_FOUND));

		ceremony.updateCeremonyState(CeremonyState.CLOSE);

		ceremonyRepository.save(ceremony);

		return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
	}

	@Transactional
	public CeremonyNotificationSettingResponseDto createCeremonyNotificationSettings(User user,
		CreateCeremonyNotificationSettingDto createCeremonyNotificationSettingDTO) {
		Set<String> admissionYears = validateAdmissionYears(createCeremonyNotificationSettingDTO);

		CeremonyNotificationSetting ceremonyNotificationSetting = CeremonyNotificationSetting.of(
			admissionYears,
			createCeremonyNotificationSettingDTO.isSetAll(),
			createCeremonyNotificationSettingDTO.isNotificationActive(),
			user);
		ceremonyNotificationSettingRepository.save(ceremonyNotificationSetting);

		return CeremonyDtoMapper.INSTANCE.toCeremonyNotificationSettingResponseDto(ceremonyNotificationSetting);
	}

	@EventListener // 기본 경조사 설정 생성 실패시, 학적 인증과 함께 롤백
	public void createDefaultCeremonyNotificationSetting(CertifiedUserCreatedEvent event) {
		User user = userRepository.findById(event.userId())
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_NOT_FOUND));

		ceremonyNotificationSettingRepository.findByUser(user)
			.orElseGet(() -> ceremonyNotificationSettingRepository.save(
				CeremonyNotificationSetting.of(
					new HashSet<>(),
					true,
					true,
					user)));
	}

	@Transactional(readOnly = true)
	public CeremonyNotificationSettingResponseDto getCeremonyNotificationSetting(User user) {
		CeremonyNotificationSetting ceremonyNotificationSetting = ceremonyNotificationSettingRepository.findByUser(user)
			.orElseThrow(
				() -> new BadRequestException(
					ErrorCode.ROW_DOES_NOT_EXIST,
					MessageUtil.CEREMONY_NOTIFICATION_SETTING_NOT_FOUND));
		return CeremonyDtoMapper.INSTANCE.toCeremonyNotificationSettingResponseDto(ceremonyNotificationSetting);
	}

	@Transactional
	public CeremonyNotificationSettingResponseDto updateUserSettings(User user,
		CreateCeremonyNotificationSettingDto createCeremonyNotificationSettingDTO) {
		CeremonyNotificationSetting ceremonyNotificationSetting = ceremonyNotificationSettingRepository.findByUser(user)
			.orElseThrow(
				() -> new BadRequestException(
					ErrorCode.ROW_DOES_NOT_EXIST,
					MessageUtil.CEREMONY_NOTIFICATION_SETTING_NOT_FOUND));

		Set<String> admissionYears = validateAdmissionYears(createCeremonyNotificationSettingDTO);

		ceremonyNotificationSetting.getSubscribedAdmissionYears().clear();
		ceremonyNotificationSetting.getSubscribedAdmissionYears().addAll(admissionYears);
		ceremonyNotificationSetting.updateIsSetAll(createCeremonyNotificationSettingDTO.isSetAll());
		ceremonyNotificationSetting.updateIsNotificationActive(
			createCeremonyNotificationSettingDTO.isNotificationActive());

		ceremonyNotificationSettingRepository.save(ceremonyNotificationSetting);

		return CeremonyDtoMapper.INSTANCE.toCeremonyNotificationSettingResponseDto(ceremonyNotificationSetting);

	}

	// 입학년도 유효성 검사 및 반환
	private Set<String> validateAdmissionYears(CreateCeremonyNotificationSettingDto dto) {
		// setAll이 true인 경우 빈 Set 반환 (검증 불필요)
		if (dto.isSetAll()) {
			return new HashSet<>();
		}

		// setAll이 false인 경우 검증 후 입력값 반환
		if (dto.getSubscribedAdmissionYears() == null || dto.getSubscribedAdmissionYears().isEmpty()) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.CEREMONY_NOTIFICATION_SUBSCRIPTION_REQUIRED);
		}

		// 학번 형식 (2자리) 검증
		for (String year : dto.getSubscribedAdmissionYears()) {
			if (!year.matches("^[0-9]{2}$")) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					MessageUtil.CEREMONY_INVALID_ADMISSION_YEAR_FORMAT);
			}
		}

		return dto.getSubscribedAdmissionYears();
	}

}
