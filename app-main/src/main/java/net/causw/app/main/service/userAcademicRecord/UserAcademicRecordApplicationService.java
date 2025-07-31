package net.causw.app.main.service.userAcademicRecord;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
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
            throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_ACADEMIC_RECORD_APPLICATION_AND_USER_NOT_MATCH);
        }

        userAcademicRecordApplication.setAcademicRecordRequestStatus(updateUserAcademicRecordApplicationStateRequestDto.getTargetAcademicRecordRequestStatus());
        if (updateUserAcademicRecordApplicationStateRequestDto.getTargetAcademicRecordRequestStatus().equals(AcademicRecordRequestStatus.REJECT)) {
            userAcademicRecordApplication.setRejectMessage(updateUserAcademicRecordApplicationStateRequestDto.getRejectMessage());
        }

        if (updateUserAcademicRecordApplicationStateRequestDto.getTargetAcademicRecordRequestStatus().equals(AcademicRecordRequestStatus.ACCEPT)) {
            targetUser.setAcademicStatus(userAcademicRecordApplication.getTargetAcademicStatus());

            if (userAcademicRecordApplication.getTargetAcademicStatus().equals(AcademicStatus.ENROLLED)) {
                targetUser.setCurrentCompletedSemester(userAcademicRecordApplication.getTargetCompletedSemester());
            }

            // 졸업생인 경우 동문수첨 프로필 생성
            if (userAcademicRecordApplication.getTargetAcademicStatus().equals(AcademicStatus.GRADUATED)) {
                userInfoRepository.save(UserInfo.of(targetUser));
            }
        }

        UserAcademicRecordLog userAcademicRecordLog = UserAcademicRecordLog.createWithApplication(
                controllerUser,
                userAcademicRecordApplication
        );

        userRepository.save(targetUser);
        userAcademicRecordApplicationRepository.save(userAcademicRecordApplication);
        userAcademicRecordLogRepository.save(userAcademicRecordLog);

        return toUserAcademicRecordApplicationResponseDto(userAcademicRecordLog);
    }



    @Transactional
    public UserAcademicRecordApplicationResponseDto createUserAcademicRecordApplication(
            User user,
            CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto,
            List<MultipartFile> imageFileList
    ) {
        validCreateUserAcademicRecordApplicationRequestDto(createUserAcademicRecordApplicationRequestDto, imageFileList);

        user = getUser(user.getId());

        // 대상 사용자의 변경 타겟 학적 상태가 재학인 경우, 대기 중인 이미 신청한 학적 정보 신청서가 있는지 확인하고, 있다면 닫음 처리
        List<UserAcademicRecordApplication> awaitUserAcademicRecordApplicationList = this.getAwaitUserAcademicRecordApplicationList(user);

        if (!awaitUserAcademicRecordApplicationList.isEmpty()) {
            User logFinalUser = user;   // final 변수로 선언하여 람다식 내에서 사용 가능하도록 함
            List<UserAcademicRecordApplication> closedAwaitUserAcademicRecordApplicationList = new ArrayList<>();
            List<UserAcademicRecordLog> priovUserAcademicRecordLogList = new ArrayList<>();
            awaitUserAcademicRecordApplicationList
                    .forEach(userAcademicRecordApplication -> {
                        // 대상 사용자 이전 신청한 학적 정보 신청서 Closed 처리
                        userAcademicRecordApplication.setAcademicRecordRequestStatus(AcademicRecordRequestStatus.CLOSE);
                        userAcademicRecordApplication.setRejectMessage(StaticValue.USER_CLOSED);
                        closedAwaitUserAcademicRecordApplicationList.add(userAcademicRecordApplication);

                        // Closed 처리에 대한 Log 생성
                        UserAcademicRecordLog userAcademicRecordLog = UserAcademicRecordLog.createWithApplication(
                                logFinalUser,
                                userAcademicRecordApplication,
                                StaticValue.USER_CLOSED
                        );
                        priovUserAcademicRecordLogList.add(userAcademicRecordLog);
                    });
            userAcademicRecordApplicationRepository.saveAll(closedAwaitUserAcademicRecordApplicationList);
            userAcademicRecordLogRepository.saveAll(priovUserAcademicRecordLogList);
        }


        // 신청한 학적 상태 변경 신청서 처리
        UserAcademicRecordLog userAcademicRecordLog;
        UserAcademicRecordApplication userAcademicRecordApplication;

        // 대상 사용자의 변경 타겟 학적 상태가 재학인 경우, 이미지 파일을 저장하고, 학적 정보 신청서를 생성
        if (createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus().equals(AcademicStatus.ENROLLED)) {
            List<UuidFile> uuidFileList = uuidFileService.saveFileList(imageFileList, FilePath.USER_ACADEMIC_RECORD_APPLICATION);

            userAcademicRecordApplication = UserAcademicRecordApplication.createWithImage(
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
        } else {
            // 대상 사용자의 변경 타겟 학적 상태가 졸업인 경우, 학적 정보 신청서를 생성하지 않고, 사용자 졸업 관련 정보 변경 후 로그만 생성
            if (createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus().equals(AcademicStatus.GRADUATED)) {
                user.setGraduationYear(createUserAcademicRecordApplicationRequestDto.getGraduationYear());
                user.setGraduationType(createUserAcademicRecordApplicationRequestDto.getGraduationType());
                userAcademicRecordLog = UserAcademicRecordLog.createWithGraduation(
                        user,
                        user,
                        createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                        createUserAcademicRecordApplicationRequestDto.getGraduationYear(),
                        createUserAcademicRecordApplicationRequestDto.getGraduationType(),
                        StaticValue.USER_APPLIED
                );
            }
            // 대상 사용자의 변경 타겟 학적 상태가 미정인 경우, 학적 정보 신청서를 생성하지 않고, 로그만 생성
            else {
                userAcademicRecordLog = UserAcademicRecordLog.create(
                        user,
                        user,
                        createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus(),
                        StaticValue.USER_APPLIED
                );
            }

            // 재학 이외의 상태가 타겟일 시, 대상 사용자의 변경 타겟 학적 상태를 변경
            user.setAcademicStatus(createUserAcademicRecordApplicationRequestDto.getTargetAcademicStatus());

            userRepository.save(user);
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

    // private method

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

    private List<UserAcademicRecordApplication> getAwaitUserAcademicRecordApplicationList(User user) {
        return userAcademicRecordApplicationRepository.findByUserAndAcademicRecordRequestStatus(user, AcademicRecordRequestStatus.AWAIT);
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
