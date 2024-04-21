package net.causw.application.locker;

import lombok.RequiredArgsConstructor;
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
import net.causw.application.spi.FlagPort;
import net.causw.application.spi.LockerLocationPort;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.application.spi.TextFieldPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.locker.LockerDomainModel;
import net.causw.domain.model.locker.LockerLocationDomainModel;
import net.causw.domain.model.enums.LockerLogAction;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.LockerExpiredAtValidator;
import net.causw.domain.validation.LockerInUseValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LockerService {
    private final LockerPort lockerPort;
    private final LockerLocationPort lockerLocationPort;
    private final LockerLogPort lockerLogPort;
    private final UserPort userPort;
    private final FlagPort flagPort;
    private final Validator validator;
    private final LockerActionFactory lockerActionFactory;
    private final TextFieldPort textFieldPort;

    @Transactional(readOnly = true)
    public LockerResponseDto findById(String id, String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        return LockerResponseDto.of(this.lockerPort.findByIdForRead(id).orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_NOT_FOUND
                        )),
                userDomainModel
        );
    }

    @Transactional
    public LockerResponseDto create(
            String creatorId,
            LockerCreateRequestDto lockerCreateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort
                .findById(lockerCreateRequestDto.getLockerLocationId())
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );

        if (this.lockerPort.findByLockerNumber(lockerCreateRequestDto.getLockerNumber()).isPresent()) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    MessageUtil.LOCKER_DUPLICATE_NUMBER
            );
        }

        LockerDomainModel lockerDomainModel = LockerDomainModel.of(
                lockerCreateRequestDto.getLockerNumber(),
                lockerLocationDomainModel
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                .validate();

        return Optional
                .of(this.lockerPort.create(lockerDomainModel))
                .map(resLockerDomainModel -> {
                    this.lockerLogPort.create(
                            resLockerDomainModel.getLockerNumber(),
                            lockerLocationDomainModel.getName(),
                            creatorDomainModel,
                            LockerLogAction.ENABLE,
                            MessageUtil.LOCKER_FIRST_CREATED
                    );
                    return LockerResponseDto.of(resLockerDomainModel, creatorDomainModel);
                })
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                ));
    }

    @Transactional
    public LockerResponseDto update(
            String updaterId,
            String lockerId,
            LockerUpdateRequestDto lockerUpdateRequestDto
    ) {
        UserDomainModel updaterDomainModel = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerDomainModel lockerDomainModel = this.lockerPort.findByIdForWrite(lockerId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(updaterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(updaterDomainModel.getRole()))
                .validate();

        return this.lockerActionFactory
                .getLockerAction(LockerLogAction.of(lockerUpdateRequestDto.getAction()))
                .updateLockerDomainModel(
                        lockerDomainModel,
                        updaterDomainModel,
                        this.lockerPort,
                        this.lockerLogPort,
                        this.flagPort,
                        this.textFieldPort
                )
                .map(resLockerDomainModel -> {
                    this.lockerLogPort.create(
                            resLockerDomainModel.getLockerNumber(),
                            resLockerDomainModel.getLockerLocation().getName(),
                            updaterDomainModel,
                            LockerLogAction.of(lockerUpdateRequestDto.getAction()),
                            lockerUpdateRequestDto.getMessage().orElse(lockerUpdateRequestDto.getAction())
                    );
                    return LockerResponseDto.of(resLockerDomainModel, updaterDomainModel);
                })
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                ));
    }

    @Transactional
    public LockerResponseDto move(
            String updaterId,
            String lockerId,
            LockerMoveRequestDto lockerMoveRequestDto
    ) {
        UserDomainModel updaterDomainModel = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerDomainModel lockerDomainModel = this.lockerPort.findByIdForWrite(lockerId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_WRONG_POSITION
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort.findById(lockerMoveRequestDto.getLocationId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_WRONG_POSITION
                )
        );

        lockerDomainModel.move(lockerLocationDomainModel);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(updaterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(updaterDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                .validate();

        return LockerResponseDto.of(this.lockerPort.updateLocation(lockerId, lockerDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.INTERNAL_SERVER_ERROR
                        )),
                updaterDomainModel
        );
    }

    @Transactional
    public LockerResponseDto delete(String deleterId, String lockerId) {
        UserDomainModel deleterDomainModel = this.userPort.findById(deleterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerDomainModel lockerDomainModel = this.lockerPort.findByIdForWrite(lockerId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(LockerInUseValidator.of(lockerDomainModel.getUser().isPresent()))
                .consistOf(UserStateValidator.of(deleterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleterDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(deleterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        this.lockerPort.delete(lockerDomainModel);

        this.lockerLogPort.create(
                lockerDomainModel.getLockerNumber(),
                lockerDomainModel.getLockerLocation().getName(),
                deleterDomainModel,
                LockerLogAction.DISABLE,
                MessageUtil.LOCKER_DELETED
        );

        return LockerResponseDto.of(lockerDomainModel, deleterDomainModel);
    }

    @Transactional(readOnly = true)
    public LockersResponseDto findByLocation(String locationId, String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerLocationDomainModel lockerLocation = this.lockerLocationPort.findById(locationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_WRONG_POSITION
                )
        );

        return LockersResponseDto.of(
                lockerLocation.getName(),
                this.lockerPort.findByLocationId(lockerLocation.getId())
                        .stream()
                        .map(lockerDomainModel -> LockerResponseDto.of(lockerDomainModel, userDomainModel))
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public LockerLocationsResponseDto findAllLocation(String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerResponseDto myLocker = null;
        if (!userDomainModel.getRole().equals(Role.ADMIN))
            myLocker = this.lockerPort.findByUserId(userId)
                    .map(lockerDomainModel -> LockerResponseDto.of(
                            lockerDomainModel,
                            userDomainModel,
                            lockerDomainModel.getLockerLocation().getName()
                    ))
                    .orElse(null);

        return LockerLocationsResponseDto.of(
                this.lockerLocationPort.findAll()
                        .stream()
                        .map(lockerLocationDomainModel -> LockerLocationResponseDto.of(
                                lockerLocationDomainModel,
                                this.lockerPort.countEnableLockerByLocation(lockerLocationDomainModel.getId()),
                                this.lockerPort.countByLocation(lockerLocationDomainModel.getId())
                        ))
                        .collect(Collectors.toList()),
                myLocker
        );
    }

    @Transactional
    public LockerLocationResponseDto createLocation(
            String creatorId,
            LockerLocationCreateRequestDto lockerLocationCreateRequestDto
    ) {
        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        if (this.lockerLocationPort.findByName(lockerLocationCreateRequestDto.getName()).isPresent()) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    MessageUtil.LOCKER_ALREADY_REGISTERED
            );
        }

        LockerLocationDomainModel lockerLocationDomainModel = LockerLocationDomainModel.of(
                lockerLocationCreateRequestDto.getName()
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(ConstraintValidator.of(lockerLocationDomainModel, this.validator))
                .validate();

        return LockerLocationResponseDto.of(
                this.lockerLocationPort.create(lockerLocationDomainModel),
                0L,
                0L
        );
    }

    @Transactional
    public LockerLocationResponseDto updateLocation(
            String updaterId,
            String locationId,
            LockerLocationUpdateRequestDto lockerLocationRequestDto
    ) {
        UserDomainModel creatorDomainModel = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort.findById(locationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_WRONG_POSITION
                )
        );

        if (!lockerLocationDomainModel.getName().equals(lockerLocationRequestDto.getName())) {
            if (this.lockerLocationPort.findByName(lockerLocationRequestDto.getName()).isPresent()) {
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        MessageUtil.LOCKER_ALREADY_REGISTERED
                );
            }
        }

        lockerLocationDomainModel.update(
                lockerLocationRequestDto.getName()
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(ConstraintValidator.of(lockerLocationDomainModel, this.validator))
                .validate();

        return LockerLocationResponseDto.of(
                this.lockerLocationPort.update(locationId, lockerLocationDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.INTERNAL_SERVER_ERROR
                        )
                ),
                this.lockerPort.countEnableLockerByLocation(lockerLocationDomainModel.getId()),
                this.lockerPort.countByLocation(lockerLocationDomainModel.getId())
        );
    }

    @Transactional
    public LockerLocationResponseDto deleteLocation(String deleterId, String lockerLocationId) {
        UserDomainModel deleterDomainModel = this.userPort.findById(deleterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort.findById(lockerLocationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_WRONG_POSITION
                )
        );

        if (this.lockerPort.countByLocation(lockerLocationDomainModel.getId()) != 0L) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.LOCKER_ALREADY_EXIST
            );
        }

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(deleterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleterDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(deleterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        this.lockerLocationPort.delete(lockerLocationDomainModel);

        return LockerLocationResponseDto.of(lockerLocationDomainModel, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public List<LockerLogResponseDto> findLog(String id) {
        LockerDomainModel locker = this.lockerPort.findByIdForRead(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOCKER_NOT_FOUND
                )
        );

        return this.lockerLogPort.findByLockerNumber(locker.getLockerNumber());
    }

    @Transactional
    public void setExpireAt(
            String requestUserId,
            LockerExpiredAtRequestDto lockerExpiredAtRequestDto
    ) {
        UserDomainModel userDomainModel = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(userDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        this.textFieldPort.findByKey(StaticValue.EXPIRED_AT)
                .ifPresentOrElse(textField -> {
                            ValidatorBucket.of()
                                    .consistOf(LockerExpiredAtValidator.of(
                                            LocalDateTime.parse(textField, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                                            lockerExpiredAtRequestDto.getExpiredAt()))
                                    .validate();

                            this.textFieldPort.update(
                                    StaticValue.EXPIRED_AT,
                                    lockerExpiredAtRequestDto.getExpiredAt().toString()
                            );
                        },
                        () -> this.textFieldPort.create(
                                StaticValue.EXPIRED_AT,
                                lockerExpiredAtRequestDto.getExpiredAt().toString())
                );
    }
    @Transactional
    public void createAllLockers(String creatorId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        LockerLocationDomainModel lockerLocationSecondFloorDomainModel = this.lockerLocationPort
                .findById("402881cd8dcaaab2018dcab2d8820000")
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );
        LockerLocationDomainModel lockerLocationThirdFloorDomainModel = this.lockerLocationPort
                .findById("402881cd8dcaaab2018dcab2fb980001")
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );
        LockerLocationDomainModel lockerLocationFourthFloorDomainModel = this.lockerLocationPort
                .findById("402881cd8dcaaab2018dcab30ea90002")
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.LOCKER_WRONG_POSITION
                        )
                );

        for (Long lockerNumber = 1L; lockerNumber <= 136; lockerNumber++) {

            LockerDomainModel lockerDomainModel = LockerDomainModel.of(
                    lockerNumber,
                    lockerLocationSecondFloorDomainModel
            );

            validatorBucket
                    .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                    .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                    .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)))
                    .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                    .validate();

            this.lockerPort.create(lockerDomainModel);

            this.lockerLogPort.create(
                    lockerNumber,
                    lockerLocationSecondFloorDomainModel.getName(),
                    creatorDomainModel,
                    LockerLogAction.ENABLE,
                    MessageUtil.LOCKER_FIRST_CREATED
            );
        }
        for (Long lockerNumber = 1L; lockerNumber <= 168; lockerNumber++) {

            LockerDomainModel lockerDomainModel = LockerDomainModel.of(
                    lockerNumber,
                    lockerLocationThirdFloorDomainModel
            );

            validatorBucket
                    .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                    .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                    .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)))
                    .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                    .validate();

            this.lockerPort.create(lockerDomainModel);

            this.lockerLogPort.create(
                    lockerNumber,
                    lockerLocationThirdFloorDomainModel.getName(),
                    creatorDomainModel,
                    LockerLogAction.ENABLE,
                    MessageUtil.LOCKER_FIRST_CREATED
            );
        }
        for (Long lockerNumber = 1L; lockerNumber <= 32; lockerNumber++) {

            LockerDomainModel lockerDomainModel = LockerDomainModel.of(
                    lockerNumber,
                    lockerLocationFourthFloorDomainModel
            );

            validatorBucket
                    .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                    .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                    .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)))
                    .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                    .validate();

            this.lockerPort.create(lockerDomainModel);

            this.lockerLogPort.create(
                    lockerNumber,
                    lockerLocationFourthFloorDomainModel.getName(),
                    creatorDomainModel,
                    LockerLogAction.ENABLE,
                    MessageUtil.LOCKER_FIRST_CREATED
            );
        }
    }
}
