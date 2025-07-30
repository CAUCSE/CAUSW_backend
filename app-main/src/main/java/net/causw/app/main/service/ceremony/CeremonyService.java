package net.causw.app.main.service.ceremony;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.ceremony.Ceremony;
import net.causw.app.main.domain.model.entity.notification.CeremonyNotificationSetting;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.repository.notification.CeremonyNotificationSettingRepository;
import net.causw.app.main.repository.ceremony.CeremonyRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.dto.ceremony.*;
import net.causw.app.main.dto.notification.CeremonyNotificationDto;
import net.causw.app.main.dto.util.dtoMapper.CeremonyDtoMapper;
import net.causw.app.main.service.notification.CeremonyNotificationService;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.service.uuidFile.UuidFileService;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyState;
import net.causw.app.main.domain.model.enums.uuidFile.FilePath;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.app.main.domain.validation.AdmissionYearsValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CeremonyService {
    private final CeremonyRepository ceremonyRepository;
    private final CeremonyNotificationService ceremonyNotificationService;
    private final UuidFileService uuidFileService;
    private final CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;
    private final PageableFactory pageableFactory;

    @Transactional
    public CeremonyResponseDto createCeremony(
            User user,
            @Valid CreateCeremonyRequestDto createCeremonyRequestDTO,
            List<MultipartFile> imageFileList
    ) {
        // 전체 알림 전송이 false인 경우, 대상 학번이 입력되었는지 검증
        if(!createCeremonyRequestDTO.getIsSetAll()) {
            if(createCeremonyRequestDTO.getTargetAdmissionYears() == null || createCeremonyRequestDTO.getTargetAdmissionYears().isEmpty()) {
                throw new BadRequestException(
                        ErrorCode.INVALID_PARAMETER,
                        "전체 알림 전송이 false인 경우, 대상 학번은 필수 입력 값입니다."
                );
            }

            // 학번 형식 (숫자 2자리) 검증
            for (String admissionYear : createCeremonyRequestDTO.getTargetAdmissionYears()) {
                if (!admissionYear.matches("^[0-9]{2}$")) {
                    throw new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            "학번은 숫자 2자리로 입력해야 합니다. (ex. 05)"
                    );
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
                uuidFileList
        );
        ceremonyRepository.save(ceremony);

        return CeremonyDtoMapper.INSTANCE.toCeremonyResponseDto(ceremony);
    }

    @Transactional(readOnly = true)
    public Page<CeremonyNotificationDto> getUserCeremonyResponses(User user, CeremonyState state, Integer pageNum) {
        Page<Ceremony> ceremonies = ceremonyRepository.findAllByUserAndCeremonyState(user, state, pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE));

        return ceremonies.map(CeremonyNotificationDto::of);
    }

    @Transactional(readOnly = true)
    public Page<CeremonyNotificationDto> getAllUserAwaitingCeremonyPage(Integer pageNum) {
        Page<Ceremony> ceremonies = ceremonyRepository.findByCeremonyState(CeremonyState.AWAIT, pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE));

        return ceremonies.map(CeremonyNotificationDto::of);
    }

    @Transactional(readOnly = true)
    public CeremonyResponseDto getCeremony(String ceremonyId, String context, User user) {
        Ceremony ceremony = ceremonyRepository.findById(ceremonyId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOT_FOUND
                )
        );

        // context에 따라 다르게 처리
        switch (context.toLowerCase()) {
            case "my":      // 내 경조사 목록
                if (!ceremony.getUser().getId().equals(user.getId())) {
                    throw new BadRequestException(
                            ErrorCode.API_NOT_ACCESSIBLE,
                            "본인의 경조사만 상세 조회할 수 있습니다."
                    );
                }
                return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
            case "admin":       // 관리자용 경조사 관리 페이지
                if(!user.getRoles().contains(Role.ADMIN)) {
                    throw new BadRequestException(
                            ErrorCode.API_NOT_ACCESSIBLE,
                            "관리자만 접근할 수 있는 경조사입니다."
                    );
                }
                return CeremonyDtoMapper.INSTANCE.toDetailedCeremonyResponseDto(ceremony);
            case "general":
            default:
                return CeremonyDtoMapper.INSTANCE.toCeremonyResponseDto(ceremony);
        }
    }

    @Transactional
    public CeremonyResponseDto updateUserCeremonyStatus(UpdateCeremonyStateRequestDto updateDto) {
        Ceremony ceremony = ceremonyRepository.findById(updateDto.getCeremonyId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOT_FOUND
                )
        );
        ceremony.updateCeremonyState(updateDto.getTargetCeremonyState());

        if(updateDto.getTargetCeremonyState() == CeremonyState.ACCEPT){
            Integer writerAdmissionYear = ceremony.getUser().getAdmissionYear();
            ceremonyNotificationService.sendByAdmissionYear(writerAdmissionYear, ceremony);
        }
        else{ //state가 reject, await, close로 바뀌는 경우(close는 별도 처리)
            ceremony.updateNote(updateDto.getRejectMessage());
            return CeremonyDtoMapper.INSTANCE.toCeremonyResponseDto(ceremony);
        }

        ceremonyRepository.save(ceremony);

        return CeremonyDtoMapper.INSTANCE.toCeremonyResponseDto(ceremony);
    }

    @Transactional
    public CeremonyResponseDto closeUserCeremonyStatus(User user, String ceremonyId) {
        Ceremony ceremony = ceremonyRepository.findByIdAndUser(ceremonyId, user).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOT_FOUND
                )
        );

        ceremony.updateCeremonyState(CeremonyState.CLOSE);

        ceremonyRepository.save(ceremony);

        return CeremonyDtoMapper.INSTANCE.toCeremonyResponseDto(ceremony);
    }


    @Transactional
    public CeremonyNotificationSettingResponseDto createCeremonyNotificationSettings(User user, CreateCeremonyNotificationSettingDto createCeremonyNotificationSettingDTO) {
        ValidatorBucket.of()
                .consistOf(AdmissionYearsValidator.of(createCeremonyNotificationSettingDTO.getSubscribedAdmissionYears()))
                .validate();
        CeremonyNotificationSetting ceremonyNotificationSetting = CeremonyNotificationSetting.of(createCeremonyNotificationSettingDTO.getSubscribedAdmissionYears(),
                createCeremonyNotificationSettingDTO.isSetAll(), createCeremonyNotificationSettingDTO.isNotificationActive(), user);
        ceremonyNotificationSettingRepository.save(ceremonyNotificationSetting);
        return CeremonyDtoMapper.INSTANCE.toCeremonyNotificationSettingResponseDto(ceremonyNotificationSetting);
    }

    @Transactional(readOnly = true)
    public CeremonyNotificationSettingResponseDto getCeremonyNotificationSetting(User user) {
        CeremonyNotificationSetting ceremonyNotificationSetting = ceremonyNotificationSettingRepository.findByUser(user).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOTIFICATION_SETTING_NOT_FOUND
                )
        );
        return CeremonyDtoMapper.INSTANCE.toCeremonyNotificationSettingResponseDto(ceremonyNotificationSetting);
    }

    @Transactional
    public CeremonyNotificationSettingResponseDto updateUserSettings(User user, CreateCeremonyNotificationSettingDto createCeremonyNotificationSettingDTO) {
        CeremonyNotificationSetting ceremonyNotificationSetting = ceremonyNotificationSettingRepository.findByUser(user).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOTIFICATION_SETTING_NOT_FOUND
                )
        );

        ceremonyNotificationSetting.getSubscribedAdmissionYears().clear();
        ceremonyNotificationSetting.getSubscribedAdmissionYears().addAll(createCeremonyNotificationSettingDTO.getSubscribedAdmissionYears());
        ceremonyNotificationSetting.updateIsSetAll(createCeremonyNotificationSettingDTO.isSetAll());
        ceremonyNotificationSetting.updateIsNotificationActive(createCeremonyNotificationSettingDTO.isNotificationActive());

        ceremonyNotificationSettingRepository.save(ceremonyNotificationSetting);

        return CeremonyDtoMapper.INSTANCE.toCeremonyNotificationSettingResponseDto(ceremonyNotificationSetting);

    }

}
