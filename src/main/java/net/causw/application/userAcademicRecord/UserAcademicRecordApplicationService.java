package net.causw.application.userAcademicRecord;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.repository.UserAcademicRecordLogRepository;
import net.causw.adapter.persistence.repository.UserAcademicRecordApplicationRepository;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAcademicRecordApplication;
import net.causw.adapter.persistence.user.UserAcademicRecordLog;
import net.causw.application.dto.userAcademicRecordApplication.*;
import net.causw.application.dto.util.UserAcademicRecordDtoMapper;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.AcademicRecordRequestStatus;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserAcademicRecordApplicationService {

    private final UserRepository userRepository;
    private final UserAcademicRecordApplicationRepository userAcademicRecordApplicationRepository;
    private final UserAcademicRecordLogRepository userAcademicRecordLogRepository;

    public Page<UserAcademicRecordListResponseDto> getAllUserAcademicRecordPage(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toUserAcademicRecordListResponseDto);
    }

    public Page<UserAcademicRecordListResponseDto> getAllUserAwaitingAcademicRecordPage(Pageable pageable) {
        return userAcademicRecordApplicationRepository.findAllByAcademicRecordRequestStatus(pageable, AcademicRecordRequestStatus.AWAIT)
                .map(userAcademicRecordApplication -> toUserAcademicRecordListResponseDto(userAcademicRecordApplication.getUser()));
    }

    public UserAcademicRecordInfoResponseDto getUserAcademicRecordInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND
                ));

        List<UserAcademicRecordLog> userAcademicRecordLogList = userAcademicRecordLogRepository.findAllByTargetUser(user);

        return toUserAcademicRecordInfoResponseDto(user, userAcademicRecordLogList);
    }

    @Transactional
    public Void updateUserAcademicRecordNote(String userId, String note) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND
                ));

        user.setAcademicStatusNote(note);

        userRepository.save(user);

        return null;
    }


    public UserAcademicRecordApplicationInfoResponseDto getUserAcademicRecordApplicationInfo(String userId, String applicationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND
                ));

        UserAcademicRecordApplication userAcademicRecordApplication = userAcademicRecordApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_NOT_FOUND
                ));

        if (!userAcademicRecordApplication.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_AND_USER_NOT_MATCH);
        }

        return toUserAcademicRecordApplicationResponseDto(userAcademicRecordApplication);
    }

    @Transactional
    public Void updateUserAcademicStatus(User controllerUser, UpdateUserAcademicStatusRequestDto updateUserAcademicStatusRequestDto) {
        User targetUser = userRepository.findById(updateUserAcademicStatusRequestDto.getTargetUserId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND
                ));

        targetUser.setAcademicStatus(updateUserAcademicStatusRequestDto.getTargetAcademicStatus());

        UserAcademicRecordLog userAcademicRecordLog = UserAcademicRecordLog.createWithNote(
                controllerUser,
                targetUser,
                updateUserAcademicStatusRequestDto.getTargetAcademicStatus(),
                MessageUtil.ADMIN_UPDATE_ACADEMIC_RECORD_MESSAGE
        );

        userRepository.save(targetUser);
        userAcademicRecordLogRepository.save(userAcademicRecordLog);

        return null;
    }

    @Transactional
    public Void updateUserAcademicRecordApplicationStatus(User controllerUser, UpdateUserAcademicRecordApplicationRequestDto updateUserAcademicRecordApplicationRequestDto) {
        User targetUser = userRepository.findById(updateUserAcademicRecordApplicationRequestDto.getTargetUserId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND
                ));

        UserAcademicRecordApplication userAcademicRecordApplication = userAcademicRecordApplicationRepository.findById(updateUserAcademicRecordApplicationRequestDto.getApplicationId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_NOT_FOUND
                ));

        if (!userAcademicRecordApplication.getUser().getId().equals(targetUser.getId())) {
            throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_AND_USER_NOT_MATCH);
        }

        userAcademicRecordApplication.updateApplication(
                updateUserAcademicRecordApplicationRequestDto.getTargetAcademicRecordRequestStatus(),
                updateUserAcademicRecordApplicationRequestDto.getRejectMessage()
        );

        if (updateUserAcademicRecordApplicationRequestDto.getTargetAcademicRecordRequestStatus().equals(AcademicRecordRequestStatus.ACCEPT)) {
            targetUser.setAcademicStatus(userAcademicRecordApplication.getTargetAcademicStatus());

            if (userAcademicRecordApplication.getTargetAcademicStatus().equals(AcademicStatus.ENROLLED)) {
                targetUser.setCurrentCompletedSemester(userAcademicRecordApplication.getTargetCompletedSemester());
            }
        }

        UserAcademicRecordLog userAcademicRecordLog;
        if (userAcademicRecordApplication.getNote().isEmpty()) {
            userAcademicRecordLog = UserAcademicRecordLog.createWithApplication(
                    controllerUser,
                    targetUser,
                    userAcademicRecordApplication.getTargetAcademicStatus(),
                    userAcademicRecordApplication,
                    updateUserAcademicRecordApplicationRequestDto.getTargetAcademicRecordRequestStatus()
            );
        } else {
            userAcademicRecordLog = UserAcademicRecordLog.createWithApplicationAndNote(
                    controllerUser,
                    targetUser,
                    userAcademicRecordApplication.getTargetAcademicStatus(),
                    userAcademicRecordApplication,
                    updateUserAcademicRecordApplicationRequestDto.getTargetAcademicRecordRequestStatus(),
                    userAcademicRecordApplication.getNote()
            );
        }

        userRepository.save(targetUser);
        userAcademicRecordApplicationRepository.save(userAcademicRecordApplication);
        userAcademicRecordLogRepository.save(userAcademicRecordLog);

        return null;
    }

    // DTO Mapper private method

    private UserAcademicRecordListResponseDto toUserAcademicRecordListResponseDto(User user) {
        return UserAcademicRecordDtoMapper.INSTANCE.toUserAcademicRecordListResponseDto(user);
    }

    private UserAcademicRecordInfoResponseDto toUserAcademicRecordInfoResponseDto(User user, List<UserAcademicRecordLog> userAcademicRecordLogList) {
        return UserAcademicRecordDtoMapper.INSTANCE.toUserAcademicRecordInfoResponseDto(
                user,
                userAcademicRecordLogList.stream()
                        .map(this::toUserAcademicRecordApplicationResponseDto)
                        .toList()
        );
    }

    private UserAcademicRecordApplicationResponseDto toUserAcademicRecordApplicationResponseDto(UserAcademicRecordLog userAcademicRecordLog) {
        return UserAcademicRecordDtoMapper.INSTANCE.toUserAcademicRecordApplicationResponseDto(userAcademicRecordLog);
    }

    private UserAcademicRecordApplicationInfoResponseDto toUserAcademicRecordApplicationResponseDto(UserAcademicRecordApplication userAcademicRecordApplication) {
        return UserAcademicRecordDtoMapper.INSTANCE.toUserAcademicRecordApplicationInfoResponseDto(userAcademicRecordApplication);
    }


}
