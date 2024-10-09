package net.causw.application.userCouncilFee;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.repository.userCouncilFee.UserCouncilFeeLogRepository;
import net.causw.adapter.persistence.repository.userCouncilFee.UserCouncilFeeRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userCouncilFee.CouncilFeeFakeUser;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFeeLog;
import net.causw.application.dto.userCouncilFee.*;
import net.causw.application.dto.util.StatusUtil;
import net.causw.application.dto.util.dtoMapper.UserCouncilFeeDtoMapper;
import net.causw.application.excel.CouncilFeeExcelService;
import net.causw.application.semester.SemesterService;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.userCouncilFee.CouncilFeeLogType;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

@MeasureTime
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

        List<String> headerStringList = List.of(
                "동문네트워크 서비스 가입 여부",
                "이메일(아이디)",
                "이름",
                "학번",
                "입학년도",
                "닉네임",
                "전공",
                "학적상태",
                "등록 완료 학기",
                "졸업 년도",
                "졸업 유형",
                "전화번호",
                "동문 네트워크 가입일",
                "납부 시점 학기",
                "납부한 학기 수",
                "학생회비 환불 여부",
                "학생회비 환불 시점",
                "잔여 학생회비 적용 학기",
                "본 학기 학생회비 적용 여부"
        );

        List<UserCouncilFeeResponseDto> userCouncilFeeResponseDtoList = userCouncilFeeRepository.findAll()
                        .stream().map(userCouncilFee -> (userCouncilFee.getIsJoinedService()) ?
                                toUserCouncilFeeResponseDto(
                                        userCouncilFee,
                                        userCouncilFee.getUser(),
                                        StatusUtil.getRestOfSemester(userCouncilFee),
                                        StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
                                ) :
                                toUserCouncilFeeResponseDtoReduced(
                                        userCouncilFee,
                                        userCouncilFee.getCouncilFeeFakeUser(),
                                        StatusUtil.getRestOfSemester(userCouncilFee),
                                        StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
                                )
                        ).toList();

        LinkedHashMap<String, List<UserCouncilFeeResponseDto>> sheetNameDataMap = new LinkedHashMap<>();
        sheetNameDataMap.put("학생회비 납부자 현황", userCouncilFeeResponseDtoList);

        councilFeeExcelService.generateExcel(
                response,
                fileName,
                headerStringList,
                sheetNameDataMap
        );
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
                    StatusUtil.getRestOfSemester(userCouncilFee),
                    StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
            );
        } else {
            return toUserCouncilFeeResponseDtoReduced(
                    userCouncilFee,
                    userCouncilFee.getCouncilFeeFakeUser(),
                    StatusUtil.getRestOfSemester(userCouncilFee),
                    StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
            );
        }
    }

    @Transactional
    public void creatUserCouncilFeeWithUser(User user, CreateUserCouncilFeeWithUserRequestDto createUserCouncilFeeWithUserRequestDto) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        User targetUser = userRepository.findById(createUserCouncilFeeWithUserRequestDto.getUserId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        if (userCouncilFeeRepository.existsByUser(targetUser)) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.USER_COUNCIL_FEE_INFO_ALREADY_EXISTS);
        }

        if (createUserCouncilFeeWithUserRequestDto.getIsRefunded() && createUserCouncilFeeWithUserRequestDto.getRefundedAt() == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.REFUND_DATE_IS_NULL);
        }

        UserCouncilFee userCouncilFee = UserCouncilFee.of(
                true,
                targetUser,
                null,
                createUserCouncilFeeWithUserRequestDto.getPaidAt(),
                createUserCouncilFeeWithUserRequestDto.getNumOfPaidSemester(),
                createUserCouncilFeeWithUserRequestDto.getIsRefunded(),
                createUserCouncilFeeWithUserRequestDto.getRefundedAt() == null ? null : createUserCouncilFeeWithUserRequestDto.getRefundedAt()
        );

        UserCouncilFeeLog userCouncilFeeLog = UserCouncilFeeLog.fromUser(
                controlledUser,
                CouncilFeeLogType.CREATE,
                userCouncilFee,
                semesterService.getCurrentSemesterEntity(),
                targetUser,
                StatusUtil.getRestOfSemester(userCouncilFee),
                StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
        );

        userCouncilFeeRepository.save(userCouncilFee);
        userCouncilFeeLogRepository.save(userCouncilFeeLog);
    }

    @Transactional
    public void creatUserCouncilFeeWithFakeUser(User user, CreateUserCouncilFeeWithFakeUserRequestDto createUserCouncilFeeRequestDto) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        if (userRepository.existsByStudentId(createUserCouncilFeeRequestDto.getStudentId())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.USER_ALREADY_EXISTS);
        }

        if (createUserCouncilFeeRequestDto.getIsRefunded() && createUserCouncilFeeRequestDto.getRefundedAt() == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.REFUND_DATE_IS_NULL);
        }

        CouncilFeeFakeUser councilFeeFakeUser = CouncilFeeFakeUser.of(
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

        UserCouncilFee userCouncilFee = UserCouncilFee.of(
                false,
                null,
                councilFeeFakeUser,
                createUserCouncilFeeRequestDto.getPaidAt(),
                createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                createUserCouncilFeeRequestDto.getIsRefunded(),
                createUserCouncilFeeRequestDto.getRefundedAt()
        );

        UserCouncilFeeLog userCouncilFeeLog = UserCouncilFeeLog.fromCouncilFeeFakeUser(
                controlledUser,
                CouncilFeeLogType.CREATE,
                userCouncilFee,
                semesterService.getCurrentSemesterEntity(),
                councilFeeFakeUser,
                StatusUtil.getRestOfSemester(userCouncilFee),
                StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
        );

        userCouncilFeeRepository.save(userCouncilFee);
        userCouncilFeeLogRepository.save(userCouncilFeeLog);
    }



    @Transactional
    public void updateUserCouncilFeeWithUser(User user, String userCouncilFeeId, CreateUserCouncilFeeWithUserRequestDto createUserCouncilFeeWithUserRequestDto) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        User targetUser = userRepository.findById(createUserCouncilFeeWithUserRequestDto.getUserId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        if (createUserCouncilFeeWithUserRequestDto.getIsRefunded() && createUserCouncilFeeWithUserRequestDto.getRefundedAt() == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.REFUND_DATE_IS_NULL);
        }


        userCouncilFee.update(
                true,
                targetUser,
                null,
                createUserCouncilFeeWithUserRequestDto.getPaidAt(),
                createUserCouncilFeeWithUserRequestDto.getNumOfPaidSemester(),
                createUserCouncilFeeWithUserRequestDto.getIsRefunded(),
                createUserCouncilFeeWithUserRequestDto.getRefundedAt()
        );

        UserCouncilFeeLog userCouncilFeeLog = UserCouncilFeeLog.fromUser(
                controlledUser,
                CouncilFeeLogType.UPDATE,
                userCouncilFee,
                semesterService.getCurrentSemesterEntity(),
                targetUser,
                StatusUtil.getRestOfSemester(userCouncilFee),
                StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
        );

        userCouncilFeeRepository.save(userCouncilFee);
        userCouncilFeeLogRepository.save(userCouncilFeeLog);
    }

    @Transactional
    public void updateUserCouncilFeeWithFakeUser(User user, String userCouncilFeeId, CreateUserCouncilFeeWithFakeUserRequestDto createUserCouncilFeeWithFakeUserRequestDto) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        if (userRepository.existsByStudentId(createUserCouncilFeeWithFakeUserRequestDto.getStudentId())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.USER_ALREADY_EXISTS);
        }

        if (createUserCouncilFeeWithFakeUserRequestDto.getIsRefunded() && createUserCouncilFeeWithFakeUserRequestDto.getRefundedAt() == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.REFUND_DATE_IS_NULL);
        }

        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        CouncilFeeFakeUser councilFeeFakeUser = userCouncilFee.getCouncilFeeFakeUser();

        councilFeeFakeUser.update(
                createUserCouncilFeeWithFakeUserRequestDto.getUserName(),
                createUserCouncilFeeWithFakeUserRequestDto.getStudentId(),
                createUserCouncilFeeWithFakeUserRequestDto.getPhoneNumber(),
                createUserCouncilFeeWithFakeUserRequestDto.getAdmissionYear(),
                createUserCouncilFeeWithFakeUserRequestDto.getMajor(),
                createUserCouncilFeeWithFakeUserRequestDto.getAcademicStatus(),
                createUserCouncilFeeWithFakeUserRequestDto.getCurrentCompletedSemester(),
                createUserCouncilFeeWithFakeUserRequestDto.getGraduationYear(),
                createUserCouncilFeeWithFakeUserRequestDto.getGraduationType()
        );

        userCouncilFee.update(
                false,
                null,
                councilFeeFakeUser,
                createUserCouncilFeeWithFakeUserRequestDto.getPaidAt(),
                createUserCouncilFeeWithFakeUserRequestDto.getNumOfPaidSemester(),
                createUserCouncilFeeWithFakeUserRequestDto.getIsRefunded(),
                createUserCouncilFeeWithFakeUserRequestDto.getRefundedAt()
        );

        UserCouncilFeeLog userCouncilFeeLog = UserCouncilFeeLog.fromCouncilFeeFakeUser(
                controlledUser,
                CouncilFeeLogType.UPDATE,
                userCouncilFee,
                semesterService.getCurrentSemesterEntity(),
                councilFeeFakeUser,
                StatusUtil.getRestOfSemester(userCouncilFee),
                StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
        );

        userCouncilFeeRepository.save(userCouncilFee);
        userCouncilFeeLogRepository.save(userCouncilFeeLog);
    }

    @Transactional
    public void deleteUserCouncilFee(User user, String userCouncilFeeId) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        UserCouncilFeeLog userCouncilFeeLog;

        Semester semester = semesterService.getCurrentSemesterEntity();

        if (userCouncilFee.getIsJoinedService()) {
            userCouncilFeeLog = UserCouncilFeeLog.fromUser(
                    controlledUser,
                    CouncilFeeLogType.DELETE,
                    userCouncilFee,
                    semester,
                    userCouncilFee.getUser(),
                    StatusUtil.getRestOfSemester(userCouncilFee),
                    StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
            );
        } else {
            userCouncilFeeLog = UserCouncilFeeLog.fromCouncilFeeFakeUser(
                    user,
                    CouncilFeeLogType.DELETE,
                    userCouncilFee,
                    semester,
                    userCouncilFee.getCouncilFeeFakeUser(),
                    StatusUtil.getRestOfSemester(userCouncilFee),
                    StatusUtil.getIsAppliedCurrentSemester(userCouncilFee)
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
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));
        if (userCouncilFee.getIsRefunded()) {
            return false;
        }

        return StatusUtil.getIsAppliedCurrentSemester(userCouncilFee);
    }

    public CurrentUserCouncilFeeResponseDto isCurrentSemesterAppliedBySelfInfo(User user) {
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        if (userCouncilFee.getIsRefunded()) {
            return toCurrentUserCouncilFeeResponseDto(userCouncilFee, 0, false);
        }

        return toCurrentUserCouncilFeeResponseDto(userCouncilFee, StatusUtil.getRestOfSemester(userCouncilFee), StatusUtil.getIsAppliedCurrentSemester(userCouncilFee));
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

    private CurrentUserCouncilFeeResponseDto toCurrentUserCouncilFeeResponseDto(UserCouncilFee userCouncilFee, Integer restOfSemester, Boolean isAppliedThisSemester) {
        return UserCouncilFeeDtoMapper.INSTANCE.toCurrentUserCouncilFeeResponseDto(userCouncilFee, restOfSemester, isAppliedThisSemester);
    }
}
