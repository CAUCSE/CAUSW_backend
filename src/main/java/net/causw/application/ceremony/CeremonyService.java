package net.causw.application.ceremony;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.ceremony.Ceremony;
import net.causw.adapter.persistence.notification.CeremonyNotificationSetting;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.repository.notification.CeremonyNotificationSettingRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.push.CeremonyRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.ceremony.*;
import net.causw.application.dto.notification.NotificationResponseDto;
import net.causw.application.dto.util.dtoMapper.CeremonyDtoMapper;
import net.causw.application.notification.CeremonyNotificationService;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.ceremony.CeremonyState;
import net.causw.domain.model.enums.notification.NoticeType;
import net.causw.domain.model.enums.uuidFile.FilePath;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.validation.AdmissionYearsValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CeremonyService {
    private final CeremonyRepository ceremonyRepository;
    private final CeremonyNotificationService ceremonyNotificationService;
    private final UuidFileService uuidFileService;
    private final CeremonyNotificationSettingRepository ceremonyNotificationSettingRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public CeremonyResponseDto createCeremony(
            User user,
            @Valid CreateCeremonyRequestDto createCeremonyRequestDTO,
            List<MultipartFile> imageFileList
    ) {
        List<UuidFile> uuidFileList = (imageFileList == null || imageFileList.isEmpty())
                ? List.of()
                : uuidFileService.saveFileList(imageFileList, FilePath.USER_ACADEMIC_RECORD_APPLICATION);

        Ceremony ceremony = Ceremony.createWithImages(
                user,
                createCeremonyRequestDTO.getCategory(),
                createCeremonyRequestDTO.getDescription(),
                createCeremonyRequestDTO.getStartDate(),
                createCeremonyRequestDTO.getEndDate(),
                uuidFileList
        );
        ceremonyRepository.save(ceremony);

        return CeremonyDtoMapper.INSTANCE.toCeremonyResponseDto(ceremony);
    }

    @Transactional(readOnly = true)
    public List<CeremonyResponseDto> getUserCeremonyResponsesDTO(User user) {
        List<Ceremony> ceremonies = ceremonyRepository.findAllByUser(user);
        return ceremonies.stream()
                .map(CeremonyDtoMapper.INSTANCE::toCeremonyResponseDto) // Assuming from method exists in DTO
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CeremonyResponseDto> getAllUserAwaitingCeremonyPage(Pageable pageable) {
        Page<Ceremony> ceremoniesPage = ceremonyRepository.findByCeremonyState(CeremonyState.AWAIT, pageable);
        return ceremoniesPage.map(CeremonyDtoMapper.INSTANCE::toCeremonyResponseDto);
    }

    @Transactional(readOnly = true)
    public CeremonyResponseDto getCeremony(String ceremonyId) {
        Ceremony ceremony = ceremonyRepository.findById(ceremonyId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOT_FOUND
                )
        );
        return CeremonyDtoMapper.INSTANCE.toCeremonyResponseDto(ceremony);
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

        //todo : 종료에 대한 처리
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
