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
        List<UuidFile> uuidFileList = uuidFileService.saveFileList(imageFileList, FilePath.USER_ACADEMIC_RECORD_APPLICATION);
        Ceremony ceremony = Ceremony.createWithImages(
                user,
                createCeremonyRequestDTO.getCategory(),
                createCeremonyRequestDTO.getDescription(),
                createCeremonyRequestDTO.getStartDate(),
                createCeremonyRequestDTO.getEndDate(),
                uuidFileList
        );
        ceremonyRepository.save(ceremony);

        return CeremonyResponseDto.from(ceremony);
    }

    @Transactional(readOnly = true)
    public List<CeremonyResponseDto> getUserCeremonyResponsesDTO(User user) {
        List<Ceremony> ceremonies = ceremonyRepository.findAllByUser(user);
        return ceremonies.stream()
                .map(CeremonyResponseDto::from) // Assuming from method exists in DTO
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CeremonyResponseDto> getAllUserAwaitingCeremonyPage(Pageable pageable) {
        Page<Ceremony> ceremoniesPage = ceremonyRepository.findByCeremonyState(CeremonyState.AWAIT, pageable);
        return ceremoniesPage.map(CeremonyResponseDto::from);
    }

    @Transactional(readOnly = true)
    public CeremonyResponseDto getCeremony(String ceremonyId) {
        Ceremony ceremony = ceremonyRepository.findById(ceremonyId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOT_FOUND
                )
        );
        return CeremonyResponseDto.from(ceremony);
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

        if (updateDto.getTargetCeremonyState() == CeremonyState.REJECT) {
            ceremony.updateNote(updateDto.getRejectMessage());
            return CeremonyResponseDto.from(ceremony);
        }
        Integer writerAdmissionYear = ceremony.getUser().getAdmissionYear();
        ceremonyNotificationService.sendByAdmissionYear(writerAdmissionYear, ceremony);
        ceremonyRepository.save(ceremony);

        return CeremonyResponseDto.from(ceremony);
    }


    @Transactional
    public CeremonyNotificationSettingResponseDto createCeremonyNotificationSettings(User user, CreateCeremonyNotificationSettingDto createCeremonyNotificationSettingDTO) {
        ValidatorBucket.of()
                .consistOf(AdmissionYearsValidator.of(createCeremonyNotificationSettingDTO.getSubscribedAdmissionYears()))
                .validate();
        CeremonyNotificationSetting ceremonyNotificationSetting = CeremonyNotificationSetting.of(createCeremonyNotificationSettingDTO.getSubscribedAdmissionYears(),
                createCeremonyNotificationSettingDTO.isSetAll(), createCeremonyNotificationSettingDTO.isNotificationActive(), user);
        ceremonyNotificationSettingRepository.save(ceremonyNotificationSetting);
        return CeremonyNotificationSettingResponseDto.from(ceremonyNotificationSetting);
    }

    @Transactional(readOnly = true)
    public CeremonyNotificationSettingResponseDto getCeremonyNotificationSetting(User user) {
        CeremonyNotificationSetting notificationSetting = ceremonyNotificationSettingRepository.findByUser(user).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOTIFICATION_SETTING_NOT_FOUND
                )
        );
        return CeremonyNotificationSettingResponseDto.from(notificationSetting);
    }

    @Transactional
    public CeremonyNotificationSettingResponseDto updateUserSettings(User user, CreateCeremonyNotificationSettingDto createCeremonyNotificationSettingDTO) {
        CeremonyNotificationSetting notificationSetting = ceremonyNotificationSettingRepository.findByUser(user).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOTIFICATION_SETTING_NOT_FOUND
                )
        );

        notificationSetting.getSubscribedAdmissionYears().clear();
        notificationSetting.getSubscribedAdmissionYears().addAll(createCeremonyNotificationSettingDTO.getSubscribedAdmissionYears());
        notificationSetting.updateIsSetAll(createCeremonyNotificationSettingDTO.isSetAll());
        notificationSetting.updateIsNotificationActive(createCeremonyNotificationSettingDTO.isNotificationActive());

        ceremonyNotificationSettingRepository.save(notificationSetting);

        return CeremonyNotificationSettingResponseDto.from(notificationSetting);
    }
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getCeremonyNotification(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndNoticeType(user, NoticeType.CEREMONY);

        return notifications.stream()
                .map(NotificationResponseDto::of)
                .collect(Collectors.toList());
    }
}
