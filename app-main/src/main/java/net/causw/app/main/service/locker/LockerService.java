package net.causw.app.main.service.locker;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.flag.Flag;
import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.locker.LockerLocation;
import net.causw.app.main.domain.model.entity.locker.LockerLog;
import net.causw.app.main.domain.model.entity.locker.LockerName;
import net.causw.app.main.dto.locker.*;
import net.causw.app.main.repository.flag.FlagRepository;
import net.causw.app.main.repository.locker.LockerLocationRepository;
import net.causw.app.main.repository.locker.LockerLogRepository;
import net.causw.app.main.repository.locker.LockerRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.service.common.CommonService;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;
import net.causw.app.main.domain.model.enums.locker.LockerLogAction;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.app.main.domain.validation.ConstraintValidator;
import net.causw.app.main.domain.validation.LockerExpiredAtValidator;
import net.causw.app.main.domain.validation.LockerInUseValidator;
import net.causw.app.main.domain.validation.UserRoleIsNoneValidator;
import net.causw.app.main.domain.validation.UserRoleValidator;
import net.causw.app.main.domain.validation.UserStateValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@MeasureTime
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerService {

    private final LockerRepository lockerRepository;
    private final UserRepository userRepository;
    private final LockerLogRepository lockerLogRepository;
    private final LockerLocationRepository lockerLocationRepository;
    private final Validator validator;
    private final LockerActionFactory lockerActionFactory;
    private final CommonService commonService;
    private final FlagRepository flagRepository;

    public LockerResponseDto findById(String id, User user) {
        return LockerResponseDto.of(lockerRepository.findByIdForRead(id).orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_NOT_FOUND
                        )),
                user
        );
    }

    @Transactional
    public LockerResponseDto create(
            User user,
            LockerCreateRequestDto lockerCreateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        Set<Role> roles = user.getRoles();

        LockerLocation lockerLocation = lockerLocationRepository.findById(lockerCreateRequestDto.getLockerLocationId())
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );

        if (lockerRepository.findByLockerNumber(lockerCreateRequestDto.getLockerNumber()).isPresent()) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    MessageUtil.LOCKER_DUPLICATE_NUMBER
            );
        }
        Locker locker = Locker.of(lockerCreateRequestDto.getLockerNumber(), true, user, lockerLocation, null);
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .consistOf(ConstraintValidator.of(locker, this.validator))
                .validate();

        lockerRepository.save(locker);
        lockerLogRepository.save(LockerLog.of(locker.getLockerNumber(), lockerLocation.getName(), user.getEmail(), user.getName(), LockerLogAction.ENABLE,
                MessageUtil.LOCKER_FIRST_CREATED));
        return LockerResponseDto.of(locker, user);
    }

    @Transactional
    public LockerResponseDto update(
            User user,
            String lockerId,
            LockerUpdateRequestDto lockerUpdateRequestDto
    ) {
        Set<Role> roles = user.getRoles();

        Locker locker = lockerRepository.findById(lockerId).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.LOCKER_NOT_FOUND
        ));

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .validate();

        locker = this.lockerActionFactory
                .getLockerAction(LockerLogAction.of(
                        lockerUpdateRequestDto.getAction()
                ))
                .updateLockerDomainModel(
                        locker,
                        user,
                        this,
                        commonService
                ).orElseThrow(() -> new InternalServerException(ErrorCode.LOCKER_ACTION_ERROR, MessageUtil.LOCKER_ACTION_ERROR));

        LockerLog lockerLog = LockerLog.of(
                locker.getLockerNumber(),
                locker.getLocation().getName(),
                user.getEmail(),
                user.getName(),
                LockerLogAction.of(
                        lockerUpdateRequestDto.getAction()
                ),
                lockerUpdateRequestDto.getMessage()
                        .orElse(lockerUpdateRequestDto.getAction())
        );

        lockerRepository.save(locker);
        lockerLogRepository.save(lockerLog);
        return LockerResponseDto.of(locker, user);
    }

    @Transactional
    public LockerResponseDto move(
            User user,
            String lockerId,
            LockerMoveRequestDto lockerMoveRequestDto
    ) {
        Set<Role> roles = user.getRoles();

        Locker locker = lockerRepository.findById(lockerId).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.LOCKER_NOT_FOUND
        ));

        LockerLocation lockerLocation = lockerLocationRepository.findById(lockerMoveRequestDto.getLockerLocationId())
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );

        locker.move(lockerLocation);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .consistOf(ConstraintValidator.of(locker, this.validator))
                .validate();

        lockerRepository.save(locker);

        return LockerResponseDto.of(locker, user);
    }

    public Optional<Locker> findByUserId(String userId) {
        return lockerRepository.findByUser_Id(userId);
    }

    @Transactional
    public LockerResponseDto delete(User user, String lockerId) {
        Set<Role> roles = user.getRoles();

        Locker locker = lockerRepository.findById(lockerId).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.LOCKER_NOT_FOUND
        ));


        ValidatorBucket.of()
                .consistOf(LockerInUseValidator.of(locker.getUser().isPresent()))
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of(Role.PRESIDENT)))
                .validate();

        lockerRepository.delete(locker);

        LockerLog lockerLog = LockerLog.of(locker.getLockerNumber(), locker.getLocation().getName(), user.getEmail(), user.getName(), LockerLogAction.DISABLE,
                MessageUtil.LOCKER_DELETED);
        lockerLogRepository.save(lockerLog);
        return LockerResponseDto.of(locker, user);
    }

    public LockersResponseDto findByLocation(String locationId, User user) {
        LockerLocation lockerLocation = lockerLocationRepository.findById(locationId)
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );

        String lockerPeriod = flagRepository.findByValue(true)
                .map(Flag::getKey)
                .orElse("NULL");

        return LockersResponseDto.of(
                lockerLocation.getName(),
                lockerPeriod,
                lockerRepository.findByLocation_IdOrderByLockerNumberAsc(lockerLocation.getId())
                        .stream()
                        .map(locker -> LockerResponseDto.of(locker, user))
                        .collect(Collectors.toList())
        );
    }

    public LockerLocationsResponseDto findAllLocation(User user) {
        LockerResponseDto myLocker = null;
        if (!user.getRoles().contains(Role.ADMIN))
            myLocker = lockerRepository.findByUser_Id(user.getId())
                    .map(locker -> LockerResponseDto.of(
                            locker,
                            user,
                            locker.getLocation().getName()
                    ))
                    .orElse(null);

        return LockerLocationsResponseDto.of(
                lockerLocationRepository.findAll()
                        .stream()
                        .map(lockerLocation -> LockerLocationResponseDto.of(
                                lockerLocation,
                                lockerRepository.countByLocationIdAndIsActiveIsTrueAndUserIdIsNull(lockerLocation.getId()),
                                lockerRepository.countByLocationId(lockerLocation.getId())
                        ))
                        .collect(Collectors.toList()),
                myLocker
        );
    }

    @Transactional
    public LockerLocationResponseDto createLocation(
            User user,
            LockerLocationCreateRequestDto lockerLocationCreateRequestDto
    ) {
        Set<Role> roles = user.getRoles();

        if (lockerLocationRepository.findByName(lockerLocationCreateRequestDto.getName()).isPresent()) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    MessageUtil.LOCKER_ALREADY_REGISTERED
            );
        }

        LockerLocation lockerLocation = LockerLocation.of(
                LockerName.valueOf(lockerLocationCreateRequestDto.getName())
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .consistOf(ConstraintValidator.of(lockerLocation, this.validator))
                .validate();
        LockerLocation location = LockerLocation.of(LockerName.valueOf(lockerLocationCreateRequestDto.getName()));

        return LockerLocationResponseDto.of(
                location,
                0L,
                0L
        );
    }

    @Transactional
    public LockerLocationResponseDto updateLocation(
            User user,
            String locationId,
            LockerLocationUpdateRequestDto lockerLocationRequestDto
    ) {
        Set<Role> roles = user.getRoles();
        LockerLocation lockerLocation = lockerLocationRepository.findById(locationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_WRONG_POSITION
                )
        );

        if (!lockerLocation.getName().equals(lockerLocationRequestDto.getName())) {
            if (lockerLocationRepository.findByName(lockerLocationRequestDto.getName()).isPresent()) {
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        MessageUtil.LOCKER_ALREADY_REGISTERED
                );
            }
        }

        lockerLocation.update(
                LockerName.valueOf(lockerLocationRequestDto.getName())
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .consistOf(ConstraintValidator.of(lockerLocation, this.validator))
                .validate();

        return LockerLocationResponseDto.of(
                lockerLocation,
                lockerRepository.countByLocationIdAndIsActiveIsTrueAndUserIdIsNull(lockerLocation.getId()),
                lockerRepository.countByLocationId(lockerLocation.getId())
        );
    }

    @Transactional
    public LockerLocationResponseDto deleteLocation(User user, String lockerLocationId) {
        Set<Role> roles = user.getRoles();
        LockerLocation lockerLocation = lockerLocationRepository.findById(lockerLocationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_WRONG_POSITION
                )
        );

        if (lockerRepository.countByLocationId(lockerLocation.getId()) != 0L) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.LOCKER_ALREADY_EXIST
            );
        }

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        lockerLocationRepository.delete(lockerLocation);

        return LockerLocationResponseDto.of(lockerLocation, 0L, 0L);
    }

    public List<LockerLogResponseDto> findLog(String id) {
        Locker locker = lockerRepository.findByIdForRead(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_NOT_FOUND
                )
        );
        return lockerLogRepository.findByLockerNumber(locker.getLockerNumber())
                .stream()
                .map(LockerLogResponseDto::from)
                .collect(Collectors.toList());
    }


    @Transactional
    public void setExpireAt(
            User user,
            LockerExpiredAtRequestDto lockerExpiredAtRequestDto
    ) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        commonService.findByKeyInTextField(StaticValue.EXPIRED_AT)
                .ifPresentOrElse(textField -> {
                            ValidatorBucket.of()
                                    // FIXME : LockerExpiredAtValidator에서 기존값보다 이전 날짜로 변경하는 것을 막을 필요가 있는지 검토 필요 (만료일, 반납에 대한 정책 정리 필요)
                                    .consistOf(LockerExpiredAtValidator.of(
                                            LocalDateTime.parse(textField, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                                            lockerExpiredAtRequestDto.getExpiredAt()))
                                    .validate();

                            commonService.updateTextField(
                                    StaticValue.EXPIRED_AT,
                                    lockerExpiredAtRequestDto.getExpiredAt().toString()
                            );
                        },
                        () -> commonService.createTextField(
                                StaticValue.EXPIRED_AT,
                                lockerExpiredAtRequestDto.getExpiredAt().toString())
                );
    }

    @Transactional
    public void setExtendPeriod(
            User user,
            LockerExtendPeriodRequestDto lockerExtendPeriodRequestDto
    ) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        // FIXME : 위의 Validator 관련 결정사항이 결정되면 여기 3개 모두 validator 적용 (현재는 아래 if문으로 처리)

        // 연장 시작일 < 연장 종료일 체크
        if (!lockerExtendPeriodRequestDto.getExtendStartAt().isBefore(lockerExtendPeriodRequestDto.getExtendEndAt())) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PERIOD,
                    MessageUtil.LOCKER_INVALID_EXTEND_PERIOD
            );
        }

        // 현재 만료일 < 다음 만료일 체크
        LocalDateTime currentExpiredAt = commonService.findByKeyInTextField(StaticValue.EXPIRED_AT)
                .map(dateString -> LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")))
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_EXPIRE_DATE_NOT_FOUND
                ));
        if (!currentExpiredAt.isBefore(lockerExtendPeriodRequestDto.getNextExpiredAt())) {
            throw new BadRequestException(
                    ErrorCode.INVALID_EXPIRE_DATE,
                    MessageUtil.LOCKER_INVALID_NEXT_EXPIRE_DATE
            );
        }

        // 연장 시작일 설정
        commonService.findByKeyInTextField(StaticValue.EXTEND_START_AT)
                .ifPresentOrElse(textField -> {
                            commonService.updateTextField(
                                    StaticValue.EXTEND_START_AT,
                                    lockerExtendPeriodRequestDto.getExtendStartAt().toString()
                            );
                        },
                        () -> commonService.createTextField(
                                StaticValue.EXTEND_START_AT,
                                lockerExtendPeriodRequestDto.getExtendStartAt().toString())
                );

        // 연장 종료일 설정
        commonService.findByKeyInTextField(StaticValue.EXTEND_END_AT)
                .ifPresentOrElse(textField -> {
                            commonService.updateTextField(
                                    StaticValue.EXTEND_END_AT,
                                    lockerExtendPeriodRequestDto.getExtendEndAt().toString()
                            );
                        },
                        () -> commonService.createTextField(
                                StaticValue.EXTEND_END_AT,
                                lockerExtendPeriodRequestDto.getExtendEndAt().toString())
                );

        // 다음 만료일 설정
        commonService.findByKeyInTextField(StaticValue.NEXT_EXPIRED_AT)
                .ifPresentOrElse(textField -> {
                            commonService.updateTextField(
                                    StaticValue.NEXT_EXPIRED_AT,
                                    lockerExtendPeriodRequestDto.getNextExpiredAt().toString()
                            );
                        },
                        () -> commonService.createTextField(
                                StaticValue.NEXT_EXPIRED_AT,
                                lockerExtendPeriodRequestDto.getNextExpiredAt().toString())
                );
    }

    @Transactional
    public void setRegisterPeriod(
            User user,
            LockerRegisterPeriodRequestDto lockerRegisterPeriodRequestDto
    ) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        // 신청 시작일 < 신청 종료일 체크
        if (!lockerRegisterPeriodRequestDto.getRegisterStartAt().isBefore(lockerRegisterPeriodRequestDto.getRegisterEndAt())) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PERIOD,
                    MessageUtil.LOCKER_INVALID_REGISTER_PERIOD
            );
        }

        // 신청 시작일 설정
        commonService.findByKeyInTextField(StaticValue.REGISTER_START_AT)
                .ifPresentOrElse(textField -> {
                            commonService.updateTextField(
                                    StaticValue.REGISTER_START_AT,
                                    lockerRegisterPeriodRequestDto.getRegisterStartAt().toString()
                            );
                        },
                        () -> commonService.createTextField(
                                StaticValue.REGISTER_START_AT,
                                lockerRegisterPeriodRequestDto.getRegisterStartAt().toString())
                );

        // 신청 종료일 설정
        commonService.findByKeyInTextField(StaticValue.REGISTER_END_AT)
                .ifPresentOrElse(textField -> {
                            commonService.updateTextField(
                                    StaticValue.REGISTER_END_AT,
                                    lockerRegisterPeriodRequestDto.getRegisterEndAt().toString()
                            );
                        },
                        () -> commonService.createTextField(
                                StaticValue.REGISTER_END_AT,
                                lockerRegisterPeriodRequestDto.getRegisterEndAt().toString())
                );
    }

    @Transactional
    public void createAllLockers(User user) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();


        LockerLocation lockerLocationSecondFloor = LockerLocation.of(LockerName.valueOf("SECOND"));
        lockerLocationRepository.save(lockerLocationSecondFloor);

        LockerLocation lockerLocationThirdFloor = LockerLocation.of(LockerName.valueOf("THIRD"));
        lockerLocationRepository.save(lockerLocationThirdFloor);

        LockerLocation lockerLocationFourthFloor = LockerLocation.of(LockerName.valueOf("FOURTH"));
        lockerLocationRepository.save(lockerLocationFourthFloor);

        createLockerByLockerLocationAndEndLockerNumber(lockerLocationSecondFloor, validatorBucket, user, 136L);
        createLockerByLockerLocationAndEndLockerNumber(lockerLocationThirdFloor, validatorBucket, user, 168L);
        createLockerByLockerLocationAndEndLockerNumber(lockerLocationFourthFloor, validatorBucket, user, 32L);
    }

    @Transactional
    public void returnExpiredLockers(User user) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        LocalDateTime now = LocalDateTime.now();

        // 만료된 사물함 조회
        List<Locker> expiredLockers = lockerRepository.findAllByExpireDateBeforeAndUserIsNotNull(now);

        for (Locker locker : expiredLockers) {
            this.returnAndSaveLocker(locker);

            LockerLog lockerLog = LockerLog.of(
                    locker.getLockerNumber(),
                    locker.getLocation().getName(),
                    user.getEmail(),
                    user.getName(),
                    LockerLogAction.RETURN,
                    MessageUtil.LOCKER_EXPIRED_ALL_RETURNED
            );
            lockerLogRepository.save(lockerLog);
        }
    }


    // private methods

    private void createLockerByLockerLocationAndEndLockerNumber(LockerLocation lockerLocationSecondFloor, ValidatorBucket validatorBucket, User user, Long endNum) {
        for (Long lockerNumber = 1L; lockerNumber <= endNum; lockerNumber++) {

            Locker locker = Locker.of(
                    lockerNumber,
                    true,
                    null,
                    lockerLocationSecondFloor,
                    null
            );
            Set<Role> roles = user.getRoles();

            validatorBucket
                    .consistOf(UserStateValidator.of(user.getState()))
                    .consistOf(UserRoleIsNoneValidator.of(roles))
                    .consistOf(UserRoleValidator.of(roles, Set.of()))
                    .consistOf(ConstraintValidator.of(locker, this.validator))
                    .validate();

            lockerRepository.save(locker);

            LockerLog lockerLog = LockerLog.of(
                    lockerNumber,
                    lockerLocationSecondFloor.getName(),
                    user.getEmail(),
                    user.getName(),
                    LockerLogAction.ENABLE,
                    MessageUtil.LOCKER_FIRST_CREATED
            );
            lockerLogRepository.save(lockerLog);
        }
    }

    @Transactional
    public void returnAndSaveLocker(Locker locker) {
        locker.returnLocker();
        lockerRepository.saveAndFlush(locker);
//        lockerRepository.flush();
    }
}
