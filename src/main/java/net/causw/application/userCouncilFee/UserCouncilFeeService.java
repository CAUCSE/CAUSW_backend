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
import net.causw.application.dto.util.dtoMapper.UserCouncilFeeDtoMapper;
import net.causw.application.excel.CouncilFeeExcelService;
import net.causw.application.semester.SemesterService;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.userCouncilFee.CouncilFeeLogType;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.policy.domain.UserCouncilFeePolicy;
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
                        .stream().map(this::toUserCouncilFeeResponseDto).toList();

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
        return userCouncilFeeRepository.findById(userCouncilFeeId)
                .map(this::toUserCouncilFeeResponseDto)
                .orElseThrow(() -> new BadRequestException(
                    ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));
    }

    @Transactional
    public void createUserCouncilFeeWithUser(User user, CreateUserCouncilFeeWithUserRequestDto createUserCouncilFeeWithUserRequestDto) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        User targetUser = userRepository.findById(createUserCouncilFeeWithUserRequestDto.getUserId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        if (userCouncilFeeRepository.existsByUser(targetUser)) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.USER_COUNCIL_FEE_INFO_ALREADY_EXISTS);
        }

        if (targetUser.getCurrentCompletedSemester() == null) {
            throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_CURRENT_COMPLETE_SEMESTER_DOES_NOT_EXIST);
        }

        if (createUserCouncilFeeWithUserRequestDto.getIsRefunded() && createUserCouncilFeeWithUserRequestDto.getRefundedAt() == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.REFUND_DATE_IS_NULL);
        }

        Semester currentSemester = semesterService.getCurrentSemesterEntity();
        int paidAt = UserCouncilFeePolicy.determineStartSemesterToApply(
            currentSemester,
            targetUser.getCurrentCompletedSemester(),
            targetUser.getAcademicStatus()
        );
        UserCouncilFee userCouncilFee = UserCouncilFee.of(
                true,
                targetUser,
                null,
                paidAt,
                createUserCouncilFeeWithUserRequestDto.getNumOfPaidSemester(),
                createUserCouncilFeeWithUserRequestDto.getIsRefunded(),
                createUserCouncilFeeWithUserRequestDto.getRefundedAt() == null ? null : createUserCouncilFeeWithUserRequestDto.getRefundedAt()
        );
        userCouncilFeeRepository.save(userCouncilFee);

        createUserCouncilFeeLog(controlledUser, CouncilFeeLogType.CREATE, userCouncilFee, currentSemester);
    }

    @Transactional
    public void creatUserCouncilFeeWithFakeUser(User user, CreateUserCouncilFeeWithFakeUserRequestDto createUserCouncilFeeRequestDto) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        if (createUserCouncilFeeRequestDto.getCurrentCompletedSemester() == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.USER_CURRENT_COMPLETE_SEMESTER_DOES_NOT_EXIST);
        }

        validateCreateRequestDtoWithFakeUser(createUserCouncilFeeRequestDto);

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

        Semester currentSemester = semesterService.getCurrentSemesterEntity();
        int paidAt = UserCouncilFeePolicy.determineStartSemesterToApply(
            currentSemester,
            councilFeeFakeUser.getCurrentCompletedSemester(),
            councilFeeFakeUser.getAcademicStatus()
        );
        UserCouncilFee userCouncilFee = UserCouncilFee.of(
                false,
                null,
                councilFeeFakeUser,
                paidAt,
                createUserCouncilFeeRequestDto.getNumOfPaidSemester(),
                createUserCouncilFeeRequestDto.getIsRefunded(),
                createUserCouncilFeeRequestDto.getRefundedAt()
        );
        userCouncilFeeRepository.save(userCouncilFee);

        createUserCouncilFeeLog(controlledUser, CouncilFeeLogType.CREATE, userCouncilFee, currentSemester);
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
        userCouncilFeeRepository.save(userCouncilFee);

        createUserCouncilFeeLog(
            controlledUser, CouncilFeeLogType.UPDATE, userCouncilFee, semesterService.getCurrentSemesterEntity());
    }

    @Transactional
    public void updateUserCouncilFeeWithFakeUser(User user, String userCouncilFeeId, CreateUserCouncilFeeWithFakeUserRequestDto createUserCouncilFeeWithFakeUserRequestDto) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        validateCreateRequestDtoWithFakeUser(createUserCouncilFeeWithFakeUserRequestDto);

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
        userCouncilFeeRepository.save(userCouncilFee);

        createUserCouncilFeeLog(
            controlledUser, CouncilFeeLogType.UPDATE, userCouncilFee, semesterService.getCurrentSemesterEntity());

    }

    @Transactional
    public void deleteUserCouncilFee(User user, String userCouncilFeeId) {
        User controlledUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));

        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findById(userCouncilFeeId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        userCouncilFeeRepository.deleteById(userCouncilFeeId);

        createUserCouncilFeeLog(
            controlledUser, CouncilFeeLogType.DELETE, userCouncilFee, semesterService.getCurrentSemesterEntity());

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

        return UserCouncilFeePolicy.isAppliedCurrentSemesterWithUser(userCouncilFee);
    }

    public CurrentUserCouncilFeeResponseDto isCurrentSemesterAppliedBySelfInfo(User user) {
        UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_COUNCIL_FEE_NOT_FOUND));

        if (userCouncilFee.getIsRefunded()) {
            return toCurrentUserCouncilFeeResponseDto(userCouncilFee, 0, false);
        }

        return toCurrentUserCouncilFeeResponseDto(
            userCouncilFee,
            UserCouncilFeePolicy.determineRemainingAppliedSemestersWithUser(userCouncilFee),
            UserCouncilFeePolicy.isAppliedCurrentSemesterWithUser(userCouncilFee));
    }

    private void createUserCouncilFeeLog(
        User controlledUser, CouncilFeeLogType councilFeeLogType,
        UserCouncilFee userCouncilFee, Semester currentSemester
    ) {
        UserCouncilFeeLog userCouncilFeeLog;

        if (userCouncilFee.getUser() != null) {
            userCouncilFeeLog = UserCouncilFeeLog.fromUser(
                controlledUser,
                councilFeeLogType,
                userCouncilFee,
                currentSemester,
                userCouncilFee.getUser(),
                UserCouncilFeePolicy.determineRemainingAppliedSemestersWithUser(userCouncilFee),
                UserCouncilFeePolicy.isAppliedCurrentSemesterWithUser(userCouncilFee)
            );
        } else {
            userCouncilFeeLog = UserCouncilFeeLog.fromCouncilFeeFakeUser(
                controlledUser,
                councilFeeLogType,
                userCouncilFee,
                currentSemester,
                userCouncilFee.getCouncilFeeFakeUser(),
                UserCouncilFeePolicy.determineRemainingAppliedSemestersWithFakeUser(userCouncilFee),
                UserCouncilFeePolicy.isAppliedCurrentSemesterWithFakeUser(userCouncilFee)
            );
        }

        userCouncilFeeLogRepository.save(userCouncilFeeLog);
    }

    private void validateCreateRequestDtoWithFakeUser(CreateUserCouncilFeeWithFakeUserRequestDto dto) {
        if (userRepository.existsByStudentId(dto.getStudentId())) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.USER_ALREADY_EXISTS);
        }

        if (dto.getIsRefunded() && dto.getRefundedAt() == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.REFUND_DATE_IS_NULL);
        }
    }

    // Dto Mapper private method
    private UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDto(UserCouncilFee userCouncilFee, User user) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeListResponseDto(userCouncilFee, user);
    }

    private UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser) {
        return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeListResponseDtoReduced(userCouncilFee, councilFeeFakeUser);
    }

    private UserCouncilFeeResponseDto toUserCouncilFeeResponseDto(UserCouncilFee userCouncilFee) {
        if (userCouncilFee.getIsJoinedService()) {
            return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeResponseDto(
                userCouncilFee,
                userCouncilFee.getUser(),
                UserCouncilFeePolicy.determineRemainingAppliedSemestersWithUser(userCouncilFee),
                UserCouncilFeePolicy.isAppliedCurrentSemesterWithUser(userCouncilFee)
            );
        } else {
            return UserCouncilFeeDtoMapper.INSTANCE.toUserCouncilFeeResponseDtoReduced(
                userCouncilFee,
                userCouncilFee.getCouncilFeeFakeUser(),
                UserCouncilFeePolicy.determineRemainingAppliedSemestersWithFakeUser(userCouncilFee),
                UserCouncilFeePolicy.isAppliedCurrentSemesterWithFakeUser(userCouncilFee)
            );
        }
    }

    private CurrentUserCouncilFeeResponseDto toCurrentUserCouncilFeeResponseDto(UserCouncilFee userCouncilFee, Integer restOfSemester, Boolean isAppliedThisSemester) {
        return UserCouncilFeeDtoMapper.INSTANCE.toCurrentUserCouncilFeeResponseDto(userCouncilFee, restOfSemester, isAppliedThisSemester);
    }
}
