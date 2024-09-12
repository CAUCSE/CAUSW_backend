package net.causw.application.userCouncilFee;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.AssertTrue;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.repository.UserCouncilFeeLogRepository;
import net.causw.adapter.persistence.repository.UserCouncilFeeRepository;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userCouncilFee.CouncilFeeFakeUser;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFeeLog;
import net.causw.application.dto.userCouncilFee.CreateUserCouncilFeeRequestDto;
import net.causw.application.dto.userCouncilFee.UserCouncilFeeListResponseDto;
import net.causw.application.dto.userCouncilFee.UserCouncilFeeResponseDto;
import net.causw.application.dto.util.UserCouncilFeeDtoMapper;
import net.causw.application.excel.CouncilFeeExcelService;
import net.causw.application.semester.SemesterService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.LogType;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCouncilFeeService {

    private final UserCouncilFeeRepository userCouncilFeeRepository;
    private final CouncilFeeExcelService councilFeeExcelService;
    private final UserRepository userRepository;
    private final UserCouncilFeeLogRepository userCouncilFeeLogRepository;
    private final SemesterService semesterService;

    public void exportUserCouncilFeeToExcel(HttpServletResponse response) {
        Semester semester = semesterService.getCurrentSemesterEntity();

        String fileName = semester.getSemesterYear().toString() + "-" + semester.getSemesterType().getValue() + "_학생회비_납부자_현황";

        List<UserCouncilFeeResponseDto> userCouncilFeeResponseDtoList = userCouncilFeeRepository.findAll()
                        .stream().map(userCouncilFee -> (userCouncilFee.getIsJoinedService()) ?
                                toUserCouncilFeeResponseDto(userCouncilFee, userCouncilFee.getUser(), getRestOfSemester(userCouncilFee), getIsAppliedCurrentSemester(userCouncilFee)) :
                                toUserCouncilFeeResponseDtoReduced(userCouncilFee, userCouncilFee.getCouncilFeeFakeUser(), getRestOfSemester(userCouncilFee), getIsAppliedCurrentSemester(userCouncilFee))
                        ).toList();

        councilFeeExcelService.generateExcel(response, fileName, userCouncilFeeResponseDtoList);
    }

    public Page<UserCouncilFeeListResponseDto> getUserCouncilFeeList(Pageable pageable) {
        return userCouncilFeeRepository.findAll(pageable)
                .map(userCouncilFee -> (userCouncilFee.getIsJoinedService()) ?
                        toUserCouncilFeeListResponseDto(userCouncilFee, userCouncilFee.getUser()) :
                        toUserCouncilFeeListResponseDtoReduced(userCouncilFee, userCouncilFee.getCouncilFeeFakeUser())
                );
    }

    public UserCouncilFeeResponseDto getUserCouncilFeeInfo(String userCouncilFeeId) {
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        if (userCouncilFee.getIsJoinedService()) {
            return toUserCouncilFeeResponseDto(
                    userCouncilFee,
                    userCouncilFee.getUser(),
                    getRestOfSemester(userCouncilFee),
                    getIsAppliedCurrentSemester(userCouncilFee)
            );
        } else {
            return toUserCouncilFeeResponseDtoReduced(
                    userCouncilFee,
                    userCouncilFee.getCouncilFeeFakeUser(),
                    getRestOfSemester(userCouncilFee),
                    getIsAppliedCurrentSemester(userCouncilFee)
            );
        }
    }

    @Transactional
    public void creatUserCouncilFee(User user, CreateUserCouncilFeeRequestDto createUserCouncilFeeRequestDto) {
        // isJoinedService가 true일 때 userId가 존재해야 합니다.
        if ( (createUserCouncilFeeRequestDto.getUserId() == null) != createUserCouncilFeeRequestDto.getIsJoinedService() ) {
            throw new BadRequestException(ErrorCode.INVALID_USER_DATA_REQUEST, MessageUtil.INVALID_USER_COUNCIL_FEE_INFO);
        }

        // userId가 존재하지 않으면 Fake User 정보 값은 유효한 값이 존재해야 하며, userId가 있으면 Fake User 정보 값들은 null이어야 합니다.
        if (createUserCouncilFeeRequestDto.getUserId() == null) {
            // userId가 없으면 다른 값들이 모두 적절하게 설정되어 있어야 함
            boolean isUserNameValid = createUserCouncilFeeRequestDto.getUserName() != null;
            boolean isStudentIdValid = createUserCouncilFeeRequestDto.getStudentId() != null;
            boolean isAdmissionYearValid = createUserCouncilFeeRequestDto.getAdmissionYear() != null && createUserCouncilFeeRequestDto.getAdmissionYear() > 0;
            boolean isMajorValid = createUserCouncilFeeRequestDto.getMajor() != null;
            boolean isAcademicStatusValid = createUserCouncilFeeRequestDto.getAcademicStatus() != null;
            if (createUserCouncilFeeRequestDto.getAcademicStatus() != null) {
                if (createUserCouncilFeeRequestDto.getAcademicStatus().equals(AcademicStatus.ENROLLED)) {
                    isAcademicStatusValid = createUserCouncilFeeRequestDto.getCurrentCompletedSemester() != null && createUserCouncilFeeRequestDto.getCurrentCompletedSemester() > 0;
                } else if (createUserCouncilFeeRequestDto.getAcademicStatus().equals(AcademicStatus.GRADUATED)) {
                    isAcademicStatusValid = createUserCouncilFeeRequestDto.getGraduationYear() != null && createUserCouncilFeeRequestDto.getGraduationYear() > 0 && createUserCouncilFeeRequestDto.getGraduationType() != null;
                }
            }
            boolean isPhoneNumberValid = createUserCouncilFeeRequestDto.getPhoneNumber() != null;

            if (!isUserNameValid || !isStudentIdValid || !isAdmissionYearValid || !isMajorValid || !isAcademicStatusValid || !isPhoneNumberValid) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO);
            }
        } else {
                // userId가 있으면 다른 값들은 null이어야 함
                boolean isUserNameNull = createUserCouncilFeeRequestDto.getUserName() == null;
                boolean isStudentIdNull = createUserCouncilFeeRequestDto.getStudentId() == null;
                boolean isAdmissionYearNull = createUserCouncilFeeRequestDto.getAdmissionYear() == null;
                boolean isMajorNull = createUserCouncilFeeRequestDto.getMajor() == null;
                boolean isAcademicStatusNull = createUserCouncilFeeRequestDto.getAcademicStatus() == null;
                boolean isCurrentCompletedSemesterNull = createUserCouncilFeeRequestDto.getCurrentCompletedSemester() == null;
                boolean isGraduationYearNull = createUserCouncilFeeRequestDto.getGraduationYear() == null;
                boolean isGraduationTypeNull = createUserCouncilFeeRequestDto.getGraduationType() == null;
                boolean isPhoneNumberNull = createUserCouncilFeeRequestDto.getPhoneNumber() == null;

                if (!isUserNameNull || !isStudentIdNull || !isAdmissionYearNull || !isMajorNull || !isAcademicStatusNull || !isCurrentCompletedSemesterNull || !isGraduationYearNull || !isGraduationTypeNull || !isPhoneNumberNull) {
                    throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_USER_COUNCIL_FEE_INFO);
                }
        }

        // isRefunded가 true일 때 refundedAt 값은 null이 아니고 자연수여야 합니다.
        if ((createUserCouncilFeeRequestDto.getIsRefunded() && createUserCouncilFeeRequestDto.getRefundedAt() == null)) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_USER_COUNCIL_FEE_INFO);
        }


        UserCouncilFee userCouncilFee;
        UserCouncilFeeLog userCouncilFeeLog;

        Semester semester = semesterService.getCurrentSemesterEntity();

        if (createUserCouncilFeeRequestDto.getIsJoinedService()) {
            User targetUser = userRepository.findById(createUserCouncilFeeRequestDto.getUserId())
                    .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

            this.validUserCouncilFeeInfo(
                    createUserCouncilFeeRequestDto.getIsJoinedService(),
                    targetUser,
                    null,
                    createUserCouncilFeeRequestDto.getPaidAt(),
                    createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                    createUserCouncilFeeRequestDto.getIsRefunded(),
                    createUserCouncilFeeRequestDto.getRefundedAt()
            );

            userCouncilFee = UserCouncilFee.of(
                    createUserCouncilFeeRequestDto.getIsJoinedService(),
                    targetUser,
                    null,
                    createUserCouncilFeeRequestDto.getPaidAt(),
                    createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                    createUserCouncilFeeRequestDto.getIsRefunded(),
                    createUserCouncilFeeRequestDto.getRefundedAt()
            );

            userCouncilFeeLog = UserCouncilFeeLog.fromUser(
                    user,
                    LogType.CREATE,
                    userCouncilFee,
                    semester,
                    targetUser,
                    getRestOfSemester(userCouncilFee),
                    getIsAppliedCurrentSemester(userCouncilFee)
            );
        } else {
            CouncilFeeFakeUser targetCouncilFeeFakeUser = CouncilFeeFakeUser.of(
                    createUserCouncilFeeRequestDto.getUserName(),
                    createUserCouncilFeeRequestDto.getStudentId(),
                    createUserCouncilFeeRequestDto.getPhoneNumber(),
                    createUserCouncilFeeRequestDto.getAdmissionYear(),
                    createUserCouncilFeeRequestDto.getMajor(),
                    createUserCouncilFeeRequestDto.getAcademicStatus(),
                    createUserCouncilFeeRequestDto.getCurrentCompletedSemester(),
                    createUserCouncilFeeRequestDto.getGraduationYear(),
                    createUserCouncilFeeRequestDto.getGraduationType()
            );

            this.validUserCouncilFeeInfo(
                    createUserCouncilFeeRequestDto.getIsJoinedService(),
                    null,
                    targetCouncilFeeFakeUser,
                    createUserCouncilFeeRequestDto.getPaidAt(),
                    createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                    createUserCouncilFeeRequestDto.getIsRefunded(),
                    createUserCouncilFeeRequestDto.getRefundedAt()
            );

            userCouncilFee = UserCouncilFee.of(
                    createUserCouncilFeeRequestDto.getIsJoinedService(),
                    null,
                    targetCouncilFeeFakeUser,
                    createUserCouncilFeeRequestDto.getPaidAt(),
                    createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                    createUserCouncilFeeRequestDto.getIsRefunded(),
                    createUserCouncilFeeRequestDto.getRefundedAt()
            );

            userCouncilFeeLog = UserCouncilFeeLog.fromCouncilFeeFakeUser(
                    user,
                    LogType.CREATE,
                    userCouncilFee,
                    semester,
                    targetCouncilFeeFakeUser,
                    getRestOfSemester(userCouncilFee),
                    getIsAppliedCurrentSemester(userCouncilFee)
            );
        }

        userCouncilFeeRepository.save(userCouncilFee);
        userCouncilFeeLogRepository.save(userCouncilFeeLog);
    }

    @Transactional
    public void updateUserCouncilFee(User user, String userCouncilFeeId, CreateUserCouncilFeeRequestDto createUserCouncilFeeRequestDto) {
        // isJoinedService가 true일 때 userId가 존재해야 합니다.
        if ( (createUserCouncilFeeRequestDto.getUserId() == null) != createUserCouncilFeeRequestDto.getIsJoinedService() ) {
            throw new BadRequestException(ErrorCode.INVALID_USER_DATA_REQUEST, MessageUtil.INVALID_USER_COUNCIL_FEE_INFO);
        }

        // userId가 존재하지 않으면 Fake User 정보 값은 유효한 값이 존재해야 하며, userId가 있으면 Fake User 정보 값들은 null이어야 합니다.
        if (createUserCouncilFeeRequestDto.getUserId() == null) {
            // userId가 없으면 다른 값들이 모두 적절하게 설정되어 있어야 함
            boolean isUserNameValid = createUserCouncilFeeRequestDto.getUserName() != null;
            boolean isStudentIdValid = createUserCouncilFeeRequestDto.getStudentId() != null;
            boolean isAdmissionYearValid = createUserCouncilFeeRequestDto.getAdmissionYear() != null && createUserCouncilFeeRequestDto.getAdmissionYear() > 0;
            boolean isMajorValid = createUserCouncilFeeRequestDto.getMajor() != null;
            boolean isAcademicStatusValid = createUserCouncilFeeRequestDto.getAcademicStatus() != null;
            if (createUserCouncilFeeRequestDto.getAcademicStatus() != null) {
                if (createUserCouncilFeeRequestDto.getAcademicStatus().equals(AcademicStatus.ENROLLED)) {
                    isAcademicStatusValid = createUserCouncilFeeRequestDto.getCurrentCompletedSemester() != null && createUserCouncilFeeRequestDto.getCurrentCompletedSemester() > 0;
                } else if (createUserCouncilFeeRequestDto.getAcademicStatus().equals(AcademicStatus.GRADUATED)) {
                    isAcademicStatusValid = createUserCouncilFeeRequestDto.getGraduationYear() != null && createUserCouncilFeeRequestDto.getGraduationYear() > 0 && createUserCouncilFeeRequestDto.getGraduationType() != null;
                }
            }
            boolean isPhoneNumberValid = createUserCouncilFeeRequestDto.getPhoneNumber() != null;

            if (!isUserNameValid || !isStudentIdValid || !isAdmissionYearValid || !isMajorValid || !isAcademicStatusValid || !isPhoneNumberValid) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO);
            }
        } else {
            // userId가 있으면 다른 값들은 null이어야 함
            boolean isUserNameNull = createUserCouncilFeeRequestDto.getUserName() == null;
            boolean isStudentIdNull = createUserCouncilFeeRequestDto.getStudentId() == null;
            boolean isAdmissionYearNull = createUserCouncilFeeRequestDto.getAdmissionYear() == null;
            boolean isMajorNull = createUserCouncilFeeRequestDto.getMajor() == null;
            boolean isAcademicStatusNull = createUserCouncilFeeRequestDto.getAcademicStatus() == null;
            boolean isCurrentCompletedSemesterNull = createUserCouncilFeeRequestDto.getCurrentCompletedSemester() == null;
            boolean isGraduationYearNull = createUserCouncilFeeRequestDto.getGraduationYear() == null;
            boolean isGraduationTypeNull = createUserCouncilFeeRequestDto.getGraduationType() == null;
            boolean isPhoneNumberNull = createUserCouncilFeeRequestDto.getPhoneNumber() == null;

            if (!isUserNameNull || !isStudentIdNull || !isAdmissionYearNull || !isMajorNull || !isAcademicStatusNull || !isCurrentCompletedSemesterNull || !isGraduationYearNull || !isGraduationTypeNull || !isPhoneNumberNull) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_USER_COUNCIL_FEE_INFO);
            }
        }

        // isRefunded가 true일 때 refundedAt 값은 null이 아니고 자연수여야 합니다.
        if ((createUserCouncilFeeRequestDto.getIsRefunded() && createUserCouncilFeeRequestDto.getRefundedAt() == null)) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_USER_COUNCIL_FEE_INFO);
        }


        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        UserCouncilFeeLog userCouncilFeeLog;

        Semester semester = semesterService.getCurrentSemesterEntity();

        if (createUserCouncilFeeRequestDto.getIsJoinedService()) {
            this.validUserCouncilFeeInfo(
                    createUserCouncilFeeRequestDto.getIsJoinedService(),
                    userRepository.findById(createUserCouncilFeeRequestDto.getUserId())
                            .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND)),
                    null,
                    createUserCouncilFeeRequestDto.getPaidAt(),
                    createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                    createUserCouncilFeeRequestDto.getIsRefunded(),
                    createUserCouncilFeeRequestDto.getRefundedAt()
            );

            userCouncilFee.update(
                    createUserCouncilFeeRequestDto.getIsJoinedService(),
                    userRepository.findById(createUserCouncilFeeRequestDto.getUserId())
                            .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND)),
                    null,
                    createUserCouncilFeeRequestDto.getPaidAt(),
                    createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                    createUserCouncilFeeRequestDto.getIsRefunded(),
                    createUserCouncilFeeRequestDto.getRefundedAt()
            );

            userCouncilFeeLog = UserCouncilFeeLog.fromUser(
                    user,
                    LogType.UPDATE,
                    userCouncilFee,
                    semester,
                    userCouncilFee.getUser(),
                    getRestOfSemester(userCouncilFee),
                    getIsAppliedCurrentSemester(userCouncilFee)
            );
        } else {
            CouncilFeeFakeUser councilFeeFakeUser = userCouncilFee.getCouncilFeeFakeUser();
            councilFeeFakeUser.update(
                    createUserCouncilFeeRequestDto.getUserName(),
                    createUserCouncilFeeRequestDto.getStudentId(),
                    createUserCouncilFeeRequestDto.getPhoneNumber(),
                    createUserCouncilFeeRequestDto.getAdmissionYear(),
                    createUserCouncilFeeRequestDto.getMajor(),
                    createUserCouncilFeeRequestDto.getAcademicStatus(),
                    createUserCouncilFeeRequestDto.getCurrentCompletedSemester(),
                    createUserCouncilFeeRequestDto.getGraduationYear(),
                    createUserCouncilFeeRequestDto.getGraduationType()
            );

            this.validUserCouncilFeeInfo(
                    createUserCouncilFeeRequestDto.getIsJoinedService(),
                    null,
                    councilFeeFakeUser,
                    createUserCouncilFeeRequestDto.getPaidAt(),
                    createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                    createUserCouncilFeeRequestDto.getIsRefunded(),
                    createUserCouncilFeeRequestDto.getRefundedAt()
            );

            userCouncilFee.update(
                    createUserCouncilFeeRequestDto.getIsJoinedService(),
                    null,
                    councilFeeFakeUser,
                    createUserCouncilFeeRequestDto.getPaidAt(),
                    createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                    createUserCouncilFeeRequestDto.getIsRefunded(),
                    createUserCouncilFeeRequestDto.getRefundedAt()
            );

            userCouncilFeeLog = UserCouncilFeeLog.fromCouncilFeeFakeUser(
                    user,
                    LogType.UPDATE,
                    userCouncilFee,
                    semester,
                    councilFeeFakeUser,
                    getRestOfSemester(userCouncilFee),
                    getIsAppliedCurrentSemester(userCouncilFee)
            );
        }

        userCouncilFeeRepository.save(userCouncilFee);
        userCouncilFeeLogRepository.save(userCouncilFeeLog);
    }

    @Transactional
    public void deleteUserCouncilFee(User user, String userCouncilFeeId) {
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        UserCouncilFeeLog userCouncilFeeLog;

        Semester semester = semesterService.getCurrentSemesterEntity();

        if (userCouncilFee.getIsJoinedService()) {
            userCouncilFeeLog = UserCouncilFeeLog.fromUser(
                    user,
                    LogType.DELETE,
                    userCouncilFee,
                    semester,
                    userCouncilFee.getUser(),
                    getRestOfSemester(userCouncilFee),
                    getIsAppliedCurrentSemester(userCouncilFee)
            );
        } else {
            userCouncilFeeLog = UserCouncilFeeLog.fromCouncilFeeFakeUser(
                    user,
                    LogType.DELETE,
                    userCouncilFee,
                    semester,
                    userCouncilFee.getCouncilFeeFakeUser(),
                    getRestOfSemester(userCouncilFee),
                    getIsAppliedCurrentSemester(userCouncilFee)
            );
        }

        userCouncilFeeRepository.deleteById(userCouncilFeeId);
        userCouncilFeeLogRepository.save(userCouncilFeeLog);
    }

    public String getUserIdByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND))
                .getId();
    }

    public Boolean isCurrentSemesterApplied(String userId) {
        return isCurrentSemesterAppliedBySelf(
                userRepository.findById(userId)
                        .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND))
        );
    }

    public Boolean isCurrentSemesterAppliedBySelf(User user) {
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUserAndIsRefunded(user, false)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        return getIsAppliedCurrentSemester(userCouncilFee);
    }

    // Private method
    private Integer getRestOfSemester(UserCouncilFee userCouncilFee) {
        Integer startOfAppliedSemester = userCouncilFee.getPaidAt();
        Integer endOfAppliedSemester = ( userCouncilFee.getIsRefunded() ) ?
                ( startOfAppliedSemester - 1 ) + userCouncilFee.getNumOfPaidSemester() :
                userCouncilFee.getRefundedAt();
        Integer restOfSemester;

        if (userCouncilFee.getIsJoinedService()) {
            restOfSemester = Math.max(endOfAppliedSemester - userCouncilFee.getUser().getCurrentCompletedSemester(), 0);
        } else {
            restOfSemester = Math.max(endOfAppliedSemester - userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester(), 0);
        }
        return restOfSemester;
    }

    private Boolean getIsAppliedCurrentSemester(UserCouncilFee userCouncilFee) {
        Integer startOfAppliedSemester = userCouncilFee.getPaidAt();
        Integer endOfAppliedSemester = ( userCouncilFee.getIsRefunded() ) ?
                ( startOfAppliedSemester - 1 ) + userCouncilFee.getNumOfPaidSemester() :
                userCouncilFee.getRefundedAt();
        Boolean isAppliedThisSemester;

        if (userCouncilFee.getIsJoinedService()) {
            isAppliedThisSemester = (startOfAppliedSemester <= userCouncilFee.getUser().getCurrentCompletedSemester()) &&
                    (userCouncilFee.getUser().getCurrentCompletedSemester() <= endOfAppliedSemester);
        } else {
            isAppliedThisSemester = (startOfAppliedSemester <= userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester()) &&
                    (userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester() <= endOfAppliedSemester);
        }
        return isAppliedThisSemester;
    }

    private void validUserCouncilFeeInfo(
            Boolean isJoinedService,
            User user,
            CouncilFeeFakeUser councilFeeFakeUser,
            Integer paidAt,
            Integer numOfPaidSemester,
            Boolean isRefunded,
            Integer refundedAt
    ) {
        if (
                (user == null ^ councilFeeFakeUser == null) ||
                        (isJoinedService && user == null) ||
                        (!isJoinedService && councilFeeFakeUser == null) ||
                        paidAt == null ||
                        numOfPaidSemester == null ||
                        isRefunded == null ||
                        (isRefunded && refundedAt == null)
        ) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_USER_COUNCIL_FEE_INFO);
        }
    }

    // Dto Mapper private method
    private UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDto(UserCouncilFee userCouncilFee, User user) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeListResponseDto(userCouncilFee, user);
    }

    private UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeListResponseDtoReduced(userCouncilFee, councilFeeFakeUser);
    }

    private UserCouncilFeeResponseDto toUserCouncilFeeResponseDto(UserCouncilFee userCouncilFee, User user, Integer restOfSemester, Boolean isAppliedThisSemester) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeResponseDto(userCouncilFee, user, restOfSemester, isAppliedThisSemester);
    }

    private UserCouncilFeeResponseDto toUserCouncilFeeResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser, Integer restOfSemester, Boolean isAppliedThisSemester) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeResponseDtoReduced(userCouncilFee, councilFeeFakeUser, restOfSemester, isAppliedThisSemester);
    }
}
