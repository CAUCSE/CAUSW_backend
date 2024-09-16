package net.causw.application.userAcademicRecord;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.repository.semester.SemesterRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.repository.userAcademicRecord.UserAcademicRecordApplicationRepository;
import net.causw.adapter.persistence.repository.userAcademicRecord.UserAcademicRecordLogRepository;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordLog;
import net.causw.application.dto.semester.CurrentSemesterResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.*;
import net.causw.application.dto.util.dtoMapper.SemesterDtoMapper;
import net.causw.application.dto.util.dtoMapper.UserAcademicRecordDtoMapper;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.AcademicRecordRequestStatus;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.FilePath;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserAcademicRecordApplicationService {

    private final UserRepository userRepository;
    private final UserAcademicRecordApplicationRepository userAcademicRecordApplicationRepository;
    private final UserAcademicRecordLogRepository userAcademicRecordLogRepository;
    private final UuidFileService uuidFileService;
    private final SemesterRepository semesterRepository;

    public CurrentSemesterResponseDto getCurrentSemesterYearAndType() {
        return toCurrentSemesterResponseDto(getCurrentSemester());
    }

    public Page<UserAcademicRecordListResponseDto> getAllUserAcademicRecordPage(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toUserAcademicRecordListResponseDto);
    }

    public Page<UserAcademicRecordApplicationListResponseDto> getAllUserAwaitingAcademicRecordPage(Pageable pageable) {
        return userAcademicRecordApplicationRepository.findAllByAcademicRecordRequestStatus(pageable, AcademicRecordRequestStatus.AWAIT)
                .map(this::toUserAcademicRecordApplicationListResponseDto);
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
        if (updateUserAcademicStatusRequestDto.getTargetAcademicStatus().equals(AcademicStatus.ENROLLED)) {
            if (updateUserAcademicStatusRequestDto.getTargetCompletedSemester() == null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.TARGET_CURRENT_COMPLETED_SEMESTER_NOT_EXIST);
            }
        } else if (updateUserAcademicStatusRequestDto.getTargetAcademicStatus().equals(AcademicStatus.GRADUATED)) {
            if (updateUserAcademicStatusRequestDto.getTargetGraduationYear() == null || updateUserAcademicStatusRequestDto.getTargetGraduationType() == null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.GRADUATION_INFORMATION_NOT_EXIST);
            }
        }

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
    public Void updateUserAcademicRecordApplicationStatus(User controllerUser, UpdateUserAcademicRecordApplicationStateRequestDto updateUserAcademicRecordApplicationStateRequestDto) {
        User targetUser = userRepository.findById(updateUserAcademicRecordApplicationStateRequestDto.getTargetUserId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND
                ));

        UserAcademicRecordApplication userAcademicRecordApplication = userAcademicRecordApplicationRepository.findById(updateUserAcademicRecordApplicationStateRequestDto.getApplicationId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_NOT_FOUND
                ));

        if (!userAcademicRecordApplication.getUser().getId().equals(targetUser.getId())) {
            throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_AND_USER_NOT_MATCH);
        }

        userAcademicRecordApplication.updateApplicationRequestStatus(
                updateUserAcademicRecordApplicationStateRequestDto.getTargetAcademicRecordRequestStatus(),
                updateUserAcademicRecordApplicationStateRequestDto.getRejectMessage()
        );

        if (updateUserAcademicRecordApplicationStateRequestDto.getTargetAcademicRecordRequestStatus().equals(AcademicRecordRequestStatus.ACCEPT)) {
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
                    updateUserAcademicRecordApplicationStateRequestDto.getTargetAcademicRecordRequestStatus()
            );
        } else {
            userAcademicRecordLog = UserAcademicRecordLog.createWithApplicationAndNote(
                    controllerUser,
                    targetUser,
                    userAcademicRecordApplication.getTargetAcademicStatus(),
                    userAcademicRecordApplication,
                    updateUserAcademicRecordApplicationStateRequestDto.getTargetAcademicRecordRequestStatus(),
                    userAcademicRecordApplication.getNote()
            );
        }

        userRepository.save(targetUser);
        userAcademicRecordApplicationRepository.save(userAcademicRecordApplication);
        userAcademicRecordLogRepository.save(userAcademicRecordLog);

        return null;
    }

    @Transactional
    public Void createUserAcademicRecordApplication(
            User user,
            CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto,
            List<MultipartFile> imageFileList
    ) {
        List<UuidFile> uuidFileList = (imageFileList.isEmpty()) ?
                new ArrayList<>() :
                uuidFileService.saveFileList(imageFileList, FilePath.USER_ACADEMIC_RECORD_APPLICATION);

        UserAcademicRecordLog userAcademicRecordLog;

        // User 엔티티가 영속성 컨텍스트에 없는 경우, merge로 다시 연결
        if (user != null) {
            user = userRepository.save(user);
        } else {
            throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND);
        }

        if (createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus().equals(AcademicStatus.ENROLLED)) {
            if (createUserAcademicRecordApplicationRequestDto.getTargetCompletedSemester() == null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.TARGET_CURRENT_COMPLETED_SEMESTER_NOT_EXIST);
            }
            UserAcademicRecordApplication userAcademicRecordApplication = UserAcademicRecordApplication.of(
                    user,
                    createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                    createUserAcademicRecordApplicationRequestDto.getTargetCompletedSemester(),
                    createUserAcademicRecordApplicationRequestDto.getNote(),
                    uuidFileList
            );

            userAcademicRecordApplicationRepository.save(userAcademicRecordApplication);

            userAcademicRecordLog = UserAcademicRecordLog.createWithApplicationAndNote(
                    user,
                    user,
                    createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                    userAcademicRecordApplication,
                    AcademicRecordRequestStatus.AWAIT,
                    StaticValue.USER_APPLIED + createUserAcademicRecordApplicationRequestDto.getNote()
            );
        } else {
            if (createUserAcademicRecordApplicationRequestDto.getNote() != null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.USER_NOTE_NOW_ALLOWED);
            }
            if (createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus().equals(AcademicStatus.GRADUATED)) {
                if (createUserAcademicRecordApplicationRequestDto.getGraduationYear() == null || createUserAcademicRecordApplicationRequestDto.getGraduationType() == null) {
                    throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.GRADUATION_INFORMATION_NOT_EXIST);
                }
                user.setGraduationYear(createUserAcademicRecordApplicationRequestDto.getGraduationYear());
                user.setGraduationType(createUserAcademicRecordApplicationRequestDto.getGraduationType());
                userAcademicRecordLog = UserAcademicRecordLog.createWithGraduationWithNote(
                        user,
                        user,
                        createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                        createUserAcademicRecordApplicationRequestDto.getGraduationYear(),
                        createUserAcademicRecordApplicationRequestDto.getGraduationType(),
                        StaticValue.USER_APPLIED
                );
            } else {
                userAcademicRecordLog = UserAcademicRecordLog.createWithNote(
                        user,
                        user,
                        createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                        StaticValue.USER_APPLIED
                );
            }
            user.setAcademicStatus(createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus());
        }

        userRepository.save(user);
        userAcademicRecordLogRepository.save(userAcademicRecordLog);

        return null;
    }

    @Transactional
    public AcademicStatus getCurrentUserAcademicRecord(User user) {
        if (user.getAcademicStatus() == null) {
            user.setAcademicStatus(AcademicStatus.UNDETERMINED);
            userRepository.save(user);
        }
        return user.getAcademicStatus();
    }

    public CurrentUserAcademicRecordApplicationResponseDto getCurrentUserAcademicRecordApplication(User user) {
        Semester semester = getCurrentSemester();

        UserAcademicRecordApplication userAcademicRecordApplication = getRecentAwaitOrRejectUserAcademicRecordApplication(user);

        return toCurrentUserAcademicRecordResponseDto(
                semester,
                userAcademicRecordApplication,
                userAcademicRecordApplication.getAcademicRecordRequestStatus().equals(AcademicRecordRequestStatus.REJECT)
        );
    }

    @Transactional
    public Void updateUserAcademicRecordApplication(
            User user,
            CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto,
            List<MultipartFile> imageFileList
    ) {
        // User 엔티티가 영속성 컨텍스트에 없는 경우, merge로 다시 연결
        if (user != null) {
            user = userRepository.save(user);
        }

        UserAcademicRecordApplication priorUserAcademicRecordApplication = getRecentAwaitOrRejectUserAcademicRecordApplication(user);

        if (!priorUserAcademicRecordApplication.getAcademicRecordRequestStatus().equals(AcademicRecordRequestStatus.REJECT)) {
            priorUserAcademicRecordApplication.updateApplicationRequestStatus(
                    AcademicRecordRequestStatus.CLOSE,
                    StaticValue.USER_CLOSED
            );
        }

        createUserAcademicRecordApplication(user, createUserAcademicRecordApplicationRequestDto, imageFileList);

        return null;
    }

    // private method
    private Semester getCurrentSemester() {
        List<Semester> activeSemesterList = semesterRepository.findAllByIsCurrent(true);
        if (activeSemesterList.isEmpty()) {
            throw new InternalServerException(ErrorCode.ROW_IS_DUPLICATED, MessageUtil.PRIOR_SEMESTER_NOT_FOUND);
        } else if (activeSemesterList.size() != 1) {
            throw new InternalServerException(ErrorCode.ROW_IS_DUPLICATED, MessageUtil.ACTIVE_SEMESTER_IS_DUPLICATED);
        }

        return activeSemesterList.get(0);
    }

    private UserAcademicRecordApplication getRecentAwaitOrRejectUserAcademicRecordApplication(User user) {
        List<UserAcademicRecordApplication> userAcademicRecordApplicationList =
                userAcademicRecordApplicationRepository.findAllByAcademicRecordRequestStatus(
                        AcademicRecordRequestStatus.AWAIT
                );

        UserAcademicRecordApplication userAcademicRecordApplication;

        if (userAcademicRecordApplicationList.size() > 1) {
            throw new InternalServerException(ErrorCode.ROW_IS_DUPLICATED, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_DUPLICATED);
        } else if (userAcademicRecordApplicationList.isEmpty()) {
            userAcademicRecordApplication = userAcademicRecordApplicationRepository.findDistinctTopByAcademicRecordRequestStatusOrderByCreatedAtDesc(AcademicRecordRequestStatus.REJECT)
                    .orElseThrow(() ->
                            new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_NOT_FOUND)
                    );
        } else {
            userAcademicRecordApplication = userAcademicRecordApplicationList.get(0);
        }

        return userAcademicRecordApplication;
    }

    // DTO Mapper private method
    private UserAcademicRecordListResponseDto toUserAcademicRecordListResponseDto(User user) {
        return UserAcademicRecordDtoMapper.INSTANCE.toUserAcademicRecordListResponseDto(user);
    }

    private UserAcademicRecordApplicationListResponseDto toUserAcademicRecordApplicationListResponseDto(UserAcademicRecordApplication userAcademicRecordApplication) {
        return UserAcademicRecordDtoMapper.INSTANCE.toUserAcademicRecordApplicationListResponseDto(userAcademicRecordApplication);
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

    private CurrentUserAcademicRecordApplicationResponseDto toCurrentUserAcademicRecordResponseDto(Semester semester, UserAcademicRecordApplication userAcademicRecordApplication, Boolean isRejected) {
        return UserAcademicRecordDtoMapper.INSTANCE.toCurrentUserAcademicRecordResponseDto(semester, userAcademicRecordApplication, isRejected);
    }

    private CurrentSemesterResponseDto toCurrentSemesterResponseDto(Semester semester) {
        return SemesterDtoMapper.INSTANCE.toCurrentSemesterResponseDto(semester);
    }


}
