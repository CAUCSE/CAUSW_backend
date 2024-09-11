package net.causw.application.userCouncilFee;

import jakarta.servlet.http.HttpServletResponse;
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
import net.causw.application.semester.SemesterService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.LogType;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCouncilFeeService {

    private final UserCouncilFeeRepository userCouncilFeeRepository;
    private final UserRepository userRepository;
    private final UserCouncilFeeLogRepository userCouncilFeeLogRepository;
    private final SemesterService semesterService;

    public void exportUserCouncilFeeToExcel(HttpServletResponse response) {
    }

    public Page<UserCouncilFeeListResponseDto> getUserCouncilFeeList(Pageable pageable) {
        return userCouncilFeeRepository.findAll(pageable)
                .map(userCouncilFee -> (userCouncilFee.getIsJoinedService()) ?
                        toUserCouncilFeeListResponseDtoReduced(userCouncilFee, userCouncilFee.getUser()) :
                        toUserCouncilFeeListResponseDtoReduced(userCouncilFee, userCouncilFee.getCouncilFeeFakeUser())
                );
    }

    public UserCouncilFeeResponseDto getUserCouncilFeeInfo(String userCouncilFeeId) {
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        if (userCouncilFee.getIsJoinedService()) {
            return toUserCouncilFeeResponseDtoReduced(
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
        UserCouncilFee userCouncilFee;
        UserCouncilFeeLog userCouncilFeeLog;

        Semester semester = semesterService.getCurrentSemesterEntity();

        if (createUserCouncilFeeRequestDto.getIsJoinedService()) {
            User targetUser = userRepository.findById(createUserCouncilFeeRequestDto.getUserId())
                    .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

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
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        UserCouncilFeeLog userCouncilFeeLog;

        Semester semester = semesterService.getCurrentSemesterEntity();

        if (createUserCouncilFeeRequestDto.getIsJoinedService()) {
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

    // Dto Mapper private method
    private UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDtoReduced(UserCouncilFee userCouncilFee, User user) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeListResponseDto(userCouncilFee, user);
    }

    private UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeListResponseDtoReduced(userCouncilFee, councilFeeFakeUser);
    }

    private UserCouncilFeeResponseDto toUserCouncilFeeResponseDtoReduced(UserCouncilFee userCouncilFee, User user, Integer restOfSemester, Boolean isAppliedThisSemester) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeResponseDto(userCouncilFee, user, restOfSemester, isAppliedThisSemester);
    }

    private UserCouncilFeeResponseDto toUserCouncilFeeResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser, Integer restOfSemester, Boolean isAppliedThisSemester) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeResponseDtoReduced(userCouncilFee, councilFeeFakeUser, restOfSemester, isAppliedThisSemester);
    }
}
