package net.causw.application;

import net.causw.application.dto.locker.LockerCreateRequestDto;
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
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.LockerLocationDomainModel;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.LockerInUseValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LockerService {
    private final LockerPort lockerPort;
    private final LockerLocationPort lockerLocationPort;
    private final LockerLogPort lockerLogPort;
    private final UserPort userPort;
    private final FlagPort flagPort;
    private final Validator validator;
    private final LockerActionFactory lockerActionFactory;

    public LockerService(
            LockerPort lockerPort,
            LockerLocationPort lockerLocationPort,
            LockerLogPort lockerLogPort,
            UserPort userPort,
            FlagPort flagPort,
            LockerActionFactory lockerActionFactory,
            Validator validator
    ) {
        this.lockerPort = lockerPort;
        this.lockerLocationPort = lockerLocationPort;
        this.lockerLogPort = lockerLogPort;
        this.userPort = userPort;
        this.flagPort = flagPort;
        this.lockerActionFactory = lockerActionFactory;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public LockerResponseDto findById(String id, String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        return LockerResponseDto.from(this.lockerPort.findById(id).orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                "사물함을 찾을 수 없습니다."
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort
                .findById(lockerCreateRequestDto.getLockerLocationId())
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                "등록된 사물함 위치가 아닙니다."
                        )
                );

        if (this.lockerPort.findByLockerNumber(lockerCreateRequestDto.getLockerNumber()).isPresent()) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    "중복된 사물함 번호입니다."
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
                            "사물함 최초 생성"
                    );
                    return LockerResponseDto.from(resLockerDomainModel, creatorDomainModel);
                })
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Exception occurred when creating locker"
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        LockerDomainModel lockerDomainModel = this.lockerPort.findById(lockerId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "사물함을 찾을 수 없습니다."
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
                        this.flagPort
                )
                .map(resLockerDomainModel -> {
                    this.lockerLogPort.create(
                            resLockerDomainModel.getLockerNumber(),
                            resLockerDomainModel.getLockerLocation().getName(),
                            updaterDomainModel,
                            LockerLogAction.of(lockerUpdateRequestDto.getAction()),
                            lockerUpdateRequestDto.getMessage().orElse(lockerUpdateRequestDto.getAction())
                    );
                    return LockerResponseDto.from(resLockerDomainModel, updaterDomainModel);
                })
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Locker id checked, but exception occurred"
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        LockerDomainModel lockerDomainModel = this.lockerPort.findById(lockerId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "사물함을 찾을 수 없습니다."
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort.findById(lockerMoveRequestDto.getLocationId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "등록된 사물함 위치가 아닙니다."
                )
        );

        lockerDomainModel.move(lockerLocationDomainModel);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(updaterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(updaterDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                .validate();

        return LockerResponseDto.from(this.lockerPort.updateLocation(lockerId, lockerDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Locker id checked, but exception occurred"
                        )),
                updaterDomainModel
        );
    }

    @Transactional
    public LockerResponseDto delete(String deleterId, String lockerId) {
        UserDomainModel deleterDomainModel = this.userPort.findById(deleterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        LockerDomainModel lockerDomainModel = this.lockerPort.findById(lockerId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "사물함을 찾을 수 없습니다."
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
                "사물함 삭제"
        );

        return LockerResponseDto.from(lockerDomainModel, deleterDomainModel);
    }

    @Transactional(readOnly = true)
    public LockersResponseDto findByLocation(String locationId, String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        LockerLocationDomainModel lockerLocation = this.lockerLocationPort.findById(locationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "등록된 사물함 위치가 아닙니다."
                )
        );

        return LockersResponseDto.of(
                lockerLocation.getName(),
                this.lockerPort.findByLocationId(lockerLocation.getId())
                        .stream()
                        .map(lockerDomainModel -> LockerResponseDto.from(lockerDomainModel, userDomainModel))
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public LockerLocationsResponseDto findAllLocation(String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        LockerResponseDto myLocker = null;
        if (!userDomainModel.getRole().equals(Role.ADMIN))
            myLocker = this.lockerPort.findByUserId(userId)
                    .map(lockerDomainModel -> LockerResponseDto.from(
                            lockerDomainModel,
                            userDomainModel,
                            lockerDomainModel.getLockerLocation().getName()
                    ))
                    .orElse(null);

        return LockerLocationsResponseDto.of(
                this.lockerLocationPort.findAll()
                        .stream()
                        .map(lockerLocationDomainModel -> LockerLocationResponseDto.from(
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        if (this.lockerLocationPort.findByName(lockerLocationCreateRequestDto.getName()).isPresent()) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    "이미 등록된 사물함 위치입니다."
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

        return LockerLocationResponseDto.from(
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort.findById(locationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "등록된 사물함 위치가 아닙니다."
                )
        );

        if (!lockerLocationDomainModel.getName().equals(lockerLocationRequestDto.getName())) {
            if (this.lockerLocationPort.findByName(lockerLocationRequestDto.getName()).isPresent()) {
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        "이미 등록된 사물함 위치 입니다."
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

        return LockerLocationResponseDto.from(
                this.lockerLocationPort.update(locationId, lockerLocationDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Locker location id checked, but exception occurred"
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort.findById(lockerLocationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "등록된 사물함 위치가 아닙니다."
                )
        );

        if (this.lockerPort.countByLocation(lockerLocationDomainModel.getId()) != 0L) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "사물함 위치에 사물함이 존재합니다."
            );
        }

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(deleterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleterDomainModel.getRole()))
                .consistOf(UserRoleValidator.of(deleterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        this.lockerLocationPort.delete(lockerLocationDomainModel);

        return LockerLocationResponseDto.from(lockerLocationDomainModel, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public List<LockerLogResponseDto> findLog(String id) {
        LockerDomainModel locker = this.lockerPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "사물함을 찾을 수 없습니다."
                )
        );

        return this.lockerLogPort.findByLockerNumber(locker.getLockerNumber());
    }
}
