package net.causw.app.main.service.userAcademicRecord;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.repository.userAcademicRecord.UserAcademicRecordApplicationRepository;
import net.causw.app.main.repository.userAcademicRecord.UserAcademicRecordLogRepository;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.semester.Semester;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordLog;
import net.causw.app.main.dto.semester.CurrentSemesterResponseDto;
import net.causw.app.main.dto.userAcademicRecordApplication.*;
import net.causw.app.main.dto.util.dtoMapper.SemesterDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.UserAcademicRecordDtoMapper;
import net.causw.app.main.repository.userInfo.UserInfoRepository;
import net.causw.app.main.service.excel.UserAcademicRecordExcelService;
import net.causw.app.main.service.semester.SemesterService;
import net.causw.app.main.service.userInfo.UserInfoService;
import net.causw.app.main.service.uuidFile.UuidFileService;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.model.enums.uuidFile.FilePath;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
@MeasureTime
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserAcademicRecordApplicationService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserAcademicRecordApplicationRepository userAcademicRecordApplicationRepository;
    private final UserAcademicRecordLogRepository userAcademicRecordLogRepository;
    private final UuidFileService uuidFileService;
    private final SemesterService semesterService;
    private final UserAcademicRecordExcelService userAcademicRecordExcelService;
    private final UserInfoService userInfoService;

    public void exportUserAcademicRecordListToExcel(HttpServletResponse response) {
        String fileName = "학적상태명단";

        List<String> headerStringList = List.of(
                "이름",
                "학번",
                "학적 상태",
                "본 학기 기준 등록 완료 학기 차수",
                "비고",
                "변환 타겟 학적 상태",
                "유저 작성 특이사항(단, 관리자 임의 수정 시 \"관리자 수정\"이라 기입)",
                "변경 날짜"
        );

        LinkedHashMap<String, List<UserAcademicRecordInfoResponseDto>> sheetDataMap = new LinkedHashMap<>();

        List<UserAcademicRecordInfoResponseDto> userAcademicRecordInfoResponseDtoList = new ArrayList<>();

        List<User> userList = userRepository.findAll();

        for (User user : userList) {
            List<UserAcademicRecordLog> userAcademicRecordLogList = userAcademicRecordLogRepository.findAllByTargetUserStudentIdAndTargetUserEmailAndTargetUserName(
                    user.getStudentId(),
                    user.getEmail(),
                    user.getName());
            userAcademicRecordInfoResponseDtoList.add(
                    toUserAcademicRecordInfoResponseDto(user, userAcademicRecordLogList)
            );
        }

        sheetDataMap.put("학적상태명단", userAcademicRecordInfoResponseDtoList);

        userAcademicRecordExcelService.generateExcel(response, fileName, headerStringList, sheetDataMap);
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
        User user = getUser(userId);

        return getUserAcademicRecordInfoResponseDto(user);
    }

    @Transactional
    public UserAcademicRecordInfoResponseDto updateUserAcademicRecordNote(String userId, String note) {
        User user = getUser(userId);

        user.setAcademicStatusNote(note);

        userRepository.save(user);

        return getUserAcademicRecordInfoResponseDto(user);
    }


    public UserAcademicRecordApplicationInfoResponseDto getUserAcademicRecordApplicationInfo(String userId, String applicationId) {
        User user = getUser(userId);

        UserAcademicRecordApplication userAcademicRecordApplication = getUserAcademicRecordApplication(applicationId);

        if (!userAcademicRecordApplication.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_AND_USER_NOT_MATCH);
        }

        return toUserAcademicRecordApplicationResponseDto(userAcademicRecordApplication);
    }



    @Transactional
    public UserAcademicRecordInfoResponseDto updateUserAcademicStatus(User controllerUser, UpdateUserAcademicStatusRequestDto updateUserAcademicStatusRequestDto) {
        validUpdateUserAcademicStatusRequestDto(updateUserAcademicStatusRequestDto);

        User targetUser = getUser(updateUserAcademicStatusRequestDto.getTargetUserId());

        targetUser.setAcademicStatus(updateUserAcademicStatusRequestDto.getTargetAcademicStatus());

        UserAcademicRecordLog userAcademicRecordLog = UserAcademicRecordLog.create(
                controllerUser,
                targetUser,
                updateUserAcademicStatusRequestDto.getTargetAcademicStatus(),
                MessageUtil.ADMIN_UPDATE_ACADEMIC_RECORD_MESSAGE
        );

        userRepository.save(targetUser);
        userAcademicRecordLogRepository.save(userAcademicRecordLog);

        return getUserAcademicRecordInfoResponseDto(targetUser);
    }

    @Transactional
    public UserAcademicRecordApplicationResponseDto updateUserAcademicRecordApplicationStatus(
            User controllerUser,
            UpdateUserAcademicRecordApplicationStateRequestDto updateUserAcademicRecordApplicationStateRequestDto
    ) {
        User targetUser = getUser(updateUserAcademicRecordApplicationStateRequestDto.getTargetUserId());
        UserAcademicRecordApplication userAcademicRecordApplication = getUserAcademicRecordApplication(updateUserAcademicRecordApplicationStateRequestDto.getApplicationId());

        if (!userAcademicRecordApplication.getUser().equals(targetUser)) {
            throw new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_AND_USER_NOT_MATCH
            );
        }

        AcademicRecordRequestStatus targetAcademicRecordRequestStatus = updateUserAcademicRecordApplicationStateRequestDto.getTargetAcademicRecordRequestStatus();

        if (targetAcademicRecordRequestStatus == AcademicRecordRequestStatus.REJECT) {
            // 거절 사유 저장
            userAcademicRecordApplication.setRejectMessage(updateUserAcademicRecordApplicationStateRequestDto.getRejectMessage());

        } else if (targetAcademicRecordRequestStatus == AcademicRecordRequestStatus.ACCEPT) {
            // 재학생의 동문수첩 기본 프로필 생성
            if (targetUser.getAcademicStatus() == AcademicStatus.UNDETERMINED) {
                userInfoService.createDefaultProfile(targetUser.getId());
            }

            // 학적 상태 및 학기 정보 변경
            targetUser.setAcademicStatus(userAcademicRecordApplication.getTargetAcademicStatus());
            targetUser.setCurrentCompletedSemester(userAcademicRecordApplication.getTargetCompletedSemester());
            userRepository.save(targetUser);
        }

        // 학적 증빙 신청서 상태 변경
        userAcademicRecordApplication.setAcademicRecordRequestStatus(targetAcademicRecordRequestStatus);
        userAcademicRecordApplicationRepository.save(userAcademicRecordApplication);


        UserAcademicRecordLog userAcademicRecordLog = UserAcademicRecordLog.createWithApplication(
                controllerUser,
                userAcademicRecordApplication
        );
        userAcademicRecordLogRepository.save(userAcademicRecordLog);

        return toUserAcademicRecordApplicationResponseDto(userAcademicRecordLog);
    }

    @Transactional
    public UserAcademicRecordApplicationResponseDto createUserAcademicRecordApplication(
            String userId,
            CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto,
            List<MultipartFile> imageFileList
    ) {
        validCreateUserAcademicRecordApplicationRequestDto(createUserAcademicRecordApplicationRequestDto, imageFileList);

        // 이전 학적 증빙 신청서 닫음 처리
        User user = getUser(userId);
        closeAwaitUserAcademicRecordApplications(user);

        AcademicStatus targetAcademicStatus = createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus();
        UserAcademicRecordLog userAcademicRecordLog;

        if (targetAcademicStatus == AcademicStatus.ENROLLED) {
            // 이미지 파일 저장
            List<UuidFile> uuidFileList = uuidFileService.saveFileList(imageFileList, FilePath.USER_ACADEMIC_RECORD_APPLICATION);

            // 학적 증빙 신청서 생성
            UserAcademicRecordApplication userAcademicRecordApplication = UserAcademicRecordApplication.createWithImage(
                    user,
                    AcademicRecordRequestStatus.AWAIT,
                    createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                    createUserAcademicRecordApplicationRequestDto.getTargetCompletedSemester(),
                    createUserAcademicRecordApplicationRequestDto.getNote(),
                    uuidFileList
            );
            userAcademicRecordApplicationRepository.save(userAcademicRecordApplication);

            userAcademicRecordLog = UserAcademicRecordLog.createWithApplication(
                    user,
                    userAcademicRecordApplication,
                    StaticValue.USER_APPLIED + createUserAcademicRecordApplicationRequestDto.getNote()
            );

        } else if (targetAcademicStatus == AcademicStatus.GRADUATED) {
            // 졸업생의 동문수첩 기본 프로필 생성
            if (user.getAcademicStatus() == AcademicStatus.UNDETERMINED) {
                userInfoService.createDefaultProfile(user.getId());
            }

            // 학적 상태 및 졸업 정보 변경
            user.setAcademicStatus(createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus());
            user.setGraduationYear(
                createUserAcademicRecordApplicationRequestDto.getGraduationYear());
            user.setGraduationType(
                createUserAcademicRecordApplicationRequestDto.getGraduationType());
            userRepository.save(user);

            userAcademicRecordLog = UserAcademicRecordLog.createWithGraduation(
                user,
                user,
                createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                createUserAcademicRecordApplicationRequestDto.getGraduationYear(),
                createUserAcademicRecordApplicationRequestDto.getGraduationType(),
                StaticValue.USER_APPLIED
            );

        } else if (targetAcademicStatus == AcademicStatus.LEAVE_OF_ABSENCE) {
            // 휴학생의 동문수첩 기본 프로필 생성
            if (user.getAcademicStatus() == AcademicStatus.UNDETERMINED) {
                userInfoService.createDefaultProfile(user.getId());
            }

            // 학적 상태 변경
            user.setAcademicStatus(createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus());
            userRepository.save(user);

            userAcademicRecordLog = UserAcademicRecordLog.create(
                user,
                user,
                createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                StaticValue.USER_APPLIED
            );

        } else {
            throw new BadRequestException(
                ErrorCode.INVALID_PARAMETER,
                MessageUtil.INVALID_TARGET_ACADEMIC_STATUS
            );
        }

        userAcademicRecordLogRepository.save(userAcademicRecordLog);

        return toUserAcademicRecordApplicationResponseDto(userAcademicRecordLog);
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
        Semester semester = semesterService.getCurrentSemesterEntity();

        UserAcademicRecordApplication userAcademicRecordApplication = getRecentAwaitOrRejectUserAcademicRecordApplication(user);

        return toCurrentUserAcademicRecordResponseDto(
                semester,
                userAcademicRecordApplication,
                userAcademicRecordApplication.getAcademicRecordRequestStatus().equals(AcademicRecordRequestStatus.REJECT)
        );
    }

    /*
    FIXME: FE 미사용으로 주석 처리

    @Transactional
    public UserAcademicRecordApplicationResponseDto updateUserAcademicRecordApplication(
            User user,
            CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto,
            List<MultipartFile> imageFileList
    ) {
        // User 엔티티가 영속성 컨텍스트에 없는 경우, merge로 다시 연결
        user = getUser(user.getId());

        UserAcademicRecordApplication priorUserAcademicRecordApplication = getRecentAwaitOrRejectUserAcademicRecordApplication(user);

        if (!priorUserAcademicRecordApplication.getAcademicRecordRequestStatus().equals(AcademicRecordRequestStatus.REJECT)) {
            priorUserAcademicRecordApplication.setAcademicRecordRequestStatus(AcademicRecordRequestStatus.CLOSE);
            priorUserAcademicRecordApplication.setRejectMessage(StaticValue.USER_CLOSED);
        }

        return createUserAcademicRecordApplication(user, createUserAcademicRecordApplicationRequestDto, imageFileList);
    }

     */

    @Transactional
    protected void closeAwaitUserAcademicRecordApplications(User user) {
        List<UserAcademicRecordApplication> awaitApplicationList = userAcademicRecordApplicationRepository
            .findByUserAndAcademicRecordRequestStatus(user, AcademicRecordRequestStatus.AWAIT);

        if (awaitApplicationList.isEmpty()) {
            return;
        }

        List<UserAcademicRecordLog> closedApplicationLogList = new ArrayList<>();

        awaitApplicationList.forEach(
            academicRecordApplication -> {
                // 대기중인 학적 증빙 신청서 닫음 처리
                academicRecordApplication.setAcademicRecordRequestStatus(AcademicRecordRequestStatus.CLOSE);
                academicRecordApplication.setRejectMessage(StaticValue.USER_CLOSED);

                UserAcademicRecordLog userAcademicRecordLog = UserAcademicRecordLog.createWithApplication(
                    user,
                    academicRecordApplication,
                    StaticValue.USER_CLOSED
                );
                closedApplicationLogList.add(userAcademicRecordLog);
            });

        userAcademicRecordLogRepository.saveAll(closedApplicationLogList);
    }

    // Entity Repository private method
    private User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));
    }

    private UserAcademicRecordInfoResponseDto getUserAcademicRecordInfoResponseDto(User user) {
        return toUserAcademicRecordInfoResponseDto(
                user,
                getUserAcademicRecordLogList(user)
        );
    }

    private List<UserAcademicRecordLog> getUserAcademicRecordLogList(User user) {
        return userAcademicRecordLogRepository.findAllByTargetUserStudentIdAndTargetUserEmailAndTargetUserName(
                user.getStudentId(),
                user.getEmail(),
                user.getName()
        );
    }

    private UserAcademicRecordApplication getUserAcademicRecordApplication(String applicationId) {
        return userAcademicRecordApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_NOT_FOUND
                ));
    }

    private UserAcademicRecordApplication getRecentAwaitOrRejectUserAcademicRecordApplication(User user) {
        List<UserAcademicRecordApplication> awaitUserAcademicRecordApplicationList =
                userAcademicRecordApplicationRepository.findAllByAcademicRecordRequestStatusAndUser(
                        AcademicRecordRequestStatus.AWAIT,
                        user
                );

        UserAcademicRecordApplication userAcademicRecordApplication;

        if (awaitUserAcademicRecordApplicationList.size() > 1) {
            throw new InternalServerException(ErrorCode.ROW_IS_DUPLICATED, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_DUPLICATED);
        } else if (awaitUserAcademicRecordApplicationList.isEmpty()) {
            userAcademicRecordApplication = userAcademicRecordApplicationRepository
                    .findDistinctTopByAcademicRecordRequestStatusAndUserOrderByCreatedAtDesc(
                            AcademicRecordRequestStatus.REJECT,
                            user
                    ).orElseThrow(() ->
                            new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_NOT_FOUND)
                    );
        } else {
            userAcademicRecordApplication = awaitUserAcademicRecordApplicationList.get(0);
        }

        return userAcademicRecordApplication;
    }

    // Validation private method
    private static void validUpdateUserAcademicStatusRequestDto(UpdateUserAcademicStatusRequestDto updateUserAcademicStatusRequestDto) {
        // 대상 사용자의 변경 타겟 학적 상태가 재학인 경우, 현재 학기가 필수 입력 값
        if (updateUserAcademicStatusRequestDto.getTargetAcademicStatus().equals(AcademicStatus.ENROLLED)) {
            if (updateUserAcademicStatusRequestDto.getTargetCompletedSemester() == null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.TARGET_CURRENT_COMPLETED_SEMESTER_NOT_EXIST);
            }
        }
        // 대상 사용자의 변경 타겟 학적 상태가 졸업인 경우, 졸업 년도와 졸업 유형이 필수 입력 값
        else if (updateUserAcademicStatusRequestDto.getTargetAcademicStatus().equals(AcademicStatus.GRADUATED)) {
            if (updateUserAcademicStatusRequestDto.getTargetGraduationYear() == null || updateUserAcademicStatusRequestDto.getTargetGraduationType() == null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.GRADUATION_INFORMATION_NOT_EXIST);
            }
        }
    }

    private void validCreateUserAcademicRecordApplicationRequestDto(
            CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto,
            List<MultipartFile> imageFileList
    ) {
        // 대상 사용자의 변경 타겟 학적 상태가 재학인 경우, 현재 학기가 필수 입력 값이며 1 이상이여야 하고, 이미지 파일이 필수 입력 값
        if (createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus().equals(AcademicStatus.ENROLLED)) {
            if (createUserAcademicRecordApplicationRequestDto.getTargetCompletedSemester() == null ||
                    createUserAcademicRecordApplicationRequestDto.getTargetCompletedSemester() < 1
            ) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.TARGET_CURRENT_COMPLETED_SEMESTER_NOT_EXIST);
            }

            if (imageFileList == null || imageFileList.isEmpty()) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.IMAGE_MUST_NOT_NULL);
            }
        }
        // 대상 사용자의 변경 타겟 학적 상태가 졸업인 경우, 졸업 년도와 졸업 유형이 필수 입력 값
        else if (createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus().equals(AcademicStatus.GRADUATED)) {
            if (createUserAcademicRecordApplicationRequestDto.getGraduationYear() == null || createUserAcademicRecordApplicationRequestDto.getGraduationType() == null) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.GRADUATION_INFORMATION_NOT_EXIST);
            }
        }
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
