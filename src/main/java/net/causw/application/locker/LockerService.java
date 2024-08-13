package net.causw.application.locker;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.locker.LockerLocation;
import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.adapter.persistence.repository.LockerLocationRepository;
import net.causw.adapter.persistence.repository.LockerLogRepository;
import net.causw.adapter.persistence.repository.LockerRepository;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.common.CommonService;
import net.causw.application.dto.locker.LockerCreateRequestDto;
import net.causw.application.dto.locker.LockerExpiredAtRequestDto;
import net.causw.application.dto.locker.LockerLocationCreateRequestDto;
import net.causw.application.dto.locker.LockerLocationResponseDto;
import net.causw.application.dto.locker.LockerLocationUpdateRequestDto;
import net.causw.application.dto.locker.LockerLocationsResponseDto;
import net.causw.application.dto.locker.LockerLogResponseDto;
import net.causw.application.dto.locker.LockerMoveRequestDto;
import net.causw.application.dto.locker.LockerResponseDto;
import net.causw.application.dto.locker.LockerUpdateRequestDto;
import net.causw.application.dto.locker.LockersResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.LockerLogAction;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.LockerExpiredAtValidator;
import net.causw.domain.validation.LockerInUseValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LockerService {
    private final LockerRepository lockerRepository;
    private final UserRepository userRepository;
    private final LockerLogRepository lockerLogRepository;
    private final LockerLocationRepository lockerLocationRepository;
    private final Validator validator;
    private final LockerActionFactory lockerActionFactory;
    private final CommonService commonService;

    @Transactional(readOnly = true)
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
            @UserValid(UserRoleValidator = true) User user,
            LockerCreateRequestDto lockerCreateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

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
                .consistOf(ConstraintValidator.of(locker, this.validator))
                .validate();

        lockerRepository.save(locker);
        lockerLogRepository.save(LockerLog.of(locker.getLockerNumber(), lockerLocation.getName(), user.getEmail(), user.getName(), LockerLogAction.ENABLE,
                MessageUtil.LOCKER_FIRST_CREATED));
        return LockerResponseDto.of(locker, user);
    }

    @Transactional
    public LockerResponseDto update(
            @UserValid User user,
            String lockerId,
            LockerUpdateRequestDto lockerUpdateRequestDto
    ) {
        Set<Role> roles = user.getRoles();

        Locker locker = lockerRepository.findById(lockerId).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.LOCKER_NOT_FOUND
        ));

        locker = this.lockerActionFactory
                .getLockerAction(LockerLogAction.of(lockerUpdateRequestDto.getAction()))
                .updateLocker(
                        locker,
                        user,
                        this,
                        commonService
                ).orElseThrow(() -> new InternalServerException(ErrorCode.LOCKER_ACTION_ERROR, MessageUtil.LOCKER_ACTION_ERROR));

        LockerLog lockerLog = LockerLog.of(locker.getLockerNumber(), locker.getLocation().getName(), user.getEmail(), user.getName(), LockerLogAction.of(lockerUpdateRequestDto.getAction()),
                lockerUpdateRequestDto.getMessage().orElse(lockerUpdateRequestDto.getAction()));

        lockerRepository.save(locker);
        lockerLogRepository.save(lockerLog);
        return LockerResponseDto.of(locker, user);
    }

    @Transactional
    public LockerResponseDto move(
            @UserValid(UserRoleValidator = true) User user,
            String lockerId,
            LockerMoveRequestDto lockerMoveRequestDto
    ) {
        Set<Role> roles = user.getRoles();

        Locker locker = lockerRepository.findById(lockerId).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.LOCKER_NOT_FOUND
        ));

        LockerLocation lockerLocation = lockerLocationRepository.findById(lockerMoveRequestDto.getLocationId())
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );

        locker.move(lockerLocation);

        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(locker, this.validator))
                .validate();

        lockerRepository.save(locker);

        return LockerResponseDto.of(locker, user);
    }

    @Transactional(readOnly = true)
    public Optional<Locker> findByUserId(String userId) {
        return lockerRepository.findByUser_Id(userId);
    }

    @Transactional
    public LockerResponseDto delete(
            @UserValid(UserRoleValidator = true, targetRoleSet = {Role.PRESIDENT}) User user,
            String lockerId
    ) {
        Locker locker = lockerRepository.findById(lockerId).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.LOCKER_NOT_FOUND
        ));
        new LockerInUseValidator().validate(locker.getUser().isPresent());

        lockerRepository.delete(locker);

        LockerLog lockerLog = LockerLog.of(locker.getLockerNumber(), locker.getLocation().getName(), user.getEmail(), user.getName(), LockerLogAction.DISABLE,
                MessageUtil.LOCKER_DELETED);
        lockerLogRepository.save(lockerLog);
        return LockerResponseDto.of(locker, user);
    }

    @Transactional(readOnly = true)
    public LockersResponseDto findByLocation(String locationId, User user) {
        LockerLocation lockerLocation = lockerLocationRepository.findById(locationId)
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );

        return LockersResponseDto.of(
                lockerLocation.getName(),
                lockerRepository.findByLocation_IdOrderByLockerNumberAsc(lockerLocation.getId())
                        .stream()
                        .map(locker -> LockerResponseDto.of(locker, user))
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
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
            @UserValid(UserRoleValidator = true) User user,
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
                lockerLocationCreateRequestDto.getName()
        );

        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(lockerLocation, this.validator))
                .validate();
        LockerLocation location = LockerLocation.of(lockerLocationCreateRequestDto.getName());

        return LockerLocationResponseDto.of(
                location,
                0L,
                0L
        );
    }

    @Transactional
    public LockerLocationResponseDto updateLocation(
            @UserValid(UserRoleValidator = true) User user,
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
                lockerLocationRequestDto.getName()
        );

        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(lockerLocation, this.validator))
                .validate();

        return LockerLocationResponseDto.of(
                lockerLocation,
                lockerRepository.countByLocationIdAndIsActiveIsTrueAndUserIdIsNull(lockerLocation.getId()),
                lockerRepository.countByLocationId(lockerLocation.getId())
        );
    }

    @Transactional
    public LockerLocationResponseDto deleteLocation(
            @UserValid(UserRoleValidator = true) User user,
            String lockerLocationId
    ) {
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

        lockerLocationRepository.delete(lockerLocation);

        return LockerLocationResponseDto.of(lockerLocation, 0L, 0L);
    }

    @Transactional(readOnly = true)
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
            @UserValid(UserRoleValidator = true) User user,
            LockerExpiredAtRequestDto lockerExpiredAtRequestDto
    ) {
        commonService.findByKeyInTextField(StaticValue.EXPIRED_AT)
                .ifPresentOrElse(textField -> {
                            new LockerExpiredAtValidator().validate(LocalDateTime.parse(textField, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")), lockerExpiredAtRequestDto.getExpiredAt());

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
    public void createAllLockers(User user) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();


        LockerLocation lockerLocationSecondFloor = LockerLocation.of("Second Floor");
        lockerLocationRepository.save(lockerLocationSecondFloor);

        LockerLocation lockerLocationThirdFloor = LockerLocation.of("Third Floor");
        lockerLocationRepository.save(lockerLocationThirdFloor);

        LockerLocation lockerLocationFourthFloor = LockerLocation.of("Fourth Floor");
        lockerLocationRepository.save(lockerLocationFourthFloor);

        createLockerByLockerLocationAndEndLockerNumber(lockerLocationSecondFloor, validatorBucket, user, 136L);
        createLockerByLockerLocationAndEndLockerNumber(lockerLocationThirdFloor, validatorBucket, user, 168L);
        createLockerByLockerLocationAndEndLockerNumber(lockerLocationFourthFloor, validatorBucket, user, 32L);
    }

    private void createLockerByLockerLocationAndEndLockerNumber(
            LockerLocation lockerLocationSecondFloor,
            ValidatorBucket validatorBucket,
            @UserValid(UserRoleValidator = true) User user,
            Long endNum
    ) {
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
                    .consistOf(ConstraintValidator.of(locker, this.validator))
                    .validate();
            new UserRoleValidator().validate(roles, Set.of());

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
}
