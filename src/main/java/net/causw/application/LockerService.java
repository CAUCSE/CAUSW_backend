package net.causw.application;

import net.causw.application.dto.LockerCreateRequestDto;
import net.causw.application.dto.LockerLocationCreateRequestDto;
import net.causw.application.dto.LockerLocationResponseDto;
import net.causw.application.dto.LockerLocationUpdateRequestDto;
import net.causw.application.dto.LockerLogDetailDto;
import net.causw.application.dto.LockerMoveRequestDto;
import net.causw.application.dto.LockerResponseDto;
import net.causw.application.dto.LockerUpdateRequestDto;
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
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LockerService {
    private final LockerPort lockerPort;
    private final LockerLocationPort lockerLocationPort;
    private final LockerLogPort lockerLogPort;
    private final UserPort userPort;
    private final Validator validator;

    public LockerService(
            LockerPort lockerPort,
            LockerLocationPort lockerLocationPort,
            LockerLogPort lockerLogPort,
            UserPort userPort,
            Validator validator
    ) {
        this.lockerPort = lockerPort;
        this.lockerLocationPort = lockerLocationPort;
        this.lockerLogPort = lockerLogPort;
        this.userPort = userPort;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public LockerResponseDto findById(String id) {
        return LockerResponseDto.from(this.lockerPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        ));
    }

    @Transactional(readOnly = false)
    public LockerResponseDto create(
            String creatorId,
            LockerCreateRequestDto lockerCreateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        validatorBucket
                .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)));



        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort
                .findById(lockerCreateRequestDto.getLockerLocationId())
                .orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.ROW_DOES_NOT_EXIST,
                            "Invalid locker location id"
                    )
                );

        LockerDomainModel lockerDomainModel = LockerDomainModel.of(
                lockerCreateRequestDto.getLockerNumber(),
                lockerLocationDomainModel
        );

        this.lockerPort.findByLockerNumber(lockerDomainModel.getLockerNumber()).ifPresent(
                name -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            "Duplicated locker number"
                    );
                }
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                .validate();

        LockerResponseDto responseDto = LockerResponseDto.from(this.lockerPort.create(lockerDomainModel));

        this.lockerLogPort.create(
                lockerDomainModel.getLockerNumber(),
                creatorDomainModel,
                LockerLogAction.ENABLE,
                "사물함 최초 생성"
        );

        return responseDto;
    }

    @Transactional(readOnly = false)
    public LockerResponseDto update(
            String updaterId,
            String lockerId,
            LockerUpdateRequestDto lockerUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel updaterDomainModel = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        LockerDomainModel lockerDomainModel = this.lockerPort.findById(lockerId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        );

        UserDomainModel lockerUserDomainModel = lockerDomainModel.getUser().orElse(null);

        // TODO: 추후 리팩터링 시 Supplier를 이용하여 Factory 구조를 적용할 것
        switch (lockerUpdateRequestDto.getAction()) {
            case REGISTER: {
                if (!lockerDomainModel.getIsActive()) {
                    throw new BadRequestException(
                            ErrorCode.CANNOT_PERFORMED,
                            "This locker is disabled"
                    );
                }

                if (lockerUserDomainModel != null) {
                    throw new BadRequestException(
                            ErrorCode.CANNOT_PERFORMED,
                            "This locker is in use"
                    );
                }

                lockerDomainModel = LockerDomainModel.of(
                        lockerId,
                        lockerDomainModel.getLockerNumber(),
                        lockerDomainModel.getIsActive(),
                        null,
                        updaterDomainModel,
                        lockerDomainModel.getLockerLocation()
                );
                break;
            }
            case RETURN: {
                if (lockerUserDomainModel == null) {
                    throw new BadRequestException(
                            ErrorCode.CANNOT_PERFORMED,
                            "This locker has no user"
                    );
                } else if (!lockerUserDomainModel.equals(updaterDomainModel)) {
                    validatorBucket
                            .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)));
                }

                lockerDomainModel = LockerDomainModel.of(
                        lockerId,
                        lockerDomainModel.getLockerNumber(),
                        lockerDomainModel.getIsActive(),
                        null,
                        null,
                        lockerDomainModel.getLockerLocation()
                );
                break;
            }
            case ENABLE: {
                validatorBucket
                        .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)));

                if (lockerDomainModel.getIsActive()) {
                    throw new BadRequestException(
                            ErrorCode.CANNOT_PERFORMED,
                            "The locker is already enable"
                    );
                }

                lockerDomainModel = LockerDomainModel.of(
                        lockerId,
                        lockerDomainModel.getLockerNumber(),
                        true,
                        null,
                        lockerDomainModel.getUser().orElse(null),
                        lockerDomainModel.getLockerLocation()
                );
                break;
            }
            case DISABLE: {
                validatorBucket
                        .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)));

                if (!lockerDomainModel.getIsActive()) {
                    throw new BadRequestException(
                            ErrorCode.CANNOT_PERFORMED,
                            "The locker is already disabled"
                    );
                }

                lockerDomainModel = LockerDomainModel.of(
                        lockerId,
                        lockerDomainModel.getLockerNumber(),
                        false,
                        null,
                        lockerDomainModel.getUser().orElse(null),
                        lockerDomainModel.getLockerLocation()
                );
                break;
            }
        }

        validatorBucket
                .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                .validate();

        LockerResponseDto lockerResponseDto = LockerResponseDto.from(this.lockerPort.update(lockerId, lockerDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Application id checked, but exception occurred"
                        )
        ));

        this.lockerLogPort.create(
                lockerDomainModel.getLockerNumber(),
                updaterDomainModel,
                lockerUpdateRequestDto.getAction(),
                lockerUpdateRequestDto.getMessage()
        );

        return lockerResponseDto;
    }

    @Transactional(readOnly = false)
    public LockerResponseDto move(
            String updaterId,
            String lockerId,
            LockerMoveRequestDto lockerMoveRequestDto
    ) {
        UserDomainModel updaterDomainModel = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        LockerDomainModel lockerDomainModel = this.lockerPort.findById(lockerId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        );

        LockerLocationDomainModel lockerLocationDomainModel = this.lockerLocationPort.findById(lockerMoveRequestDto.getLocationId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker location id"
                )
        );

        lockerDomainModel = LockerDomainModel.of(
                lockerId,
                lockerDomainModel.getLockerNumber(),
                false,
                null,
                lockerDomainModel.getUser().orElse(null),
                lockerLocationDomainModel
        );

        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(ConstraintValidator.of(lockerDomainModel, this.validator))
                .validate();

        return LockerResponseDto.from(this.lockerPort.updateLocation(lockerId, lockerDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }



    @Transactional(readOnly = true)
    public List<LockerResponseDto> findByLocation(String locationId) {
        LockerLocationDomainModel lockerLocation = this.lockerLocationPort.findById(locationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker location id"
                )
        );

        return this.lockerPort.findByLocationId(lockerLocation.getId())
                .stream()
                .map(LockerResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LockerLocationResponseDto> findAllLocation() {
        return this.lockerLocationPort.findAll()
                .stream()
                .map(
                        (lockerLocationDomainModel) -> LockerLocationResponseDto.from(
                                lockerLocationDomainModel,
                                this.lockerPort.getEnableLockerCountByLocation(lockerLocationDomainModel.getId()),
                                this.lockerPort.getLockerCountByLocation(lockerLocationDomainModel.getId())
                        )
                )
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public LockerLocationResponseDto createLocation(
            String creatorId,
            LockerLocationCreateRequestDto lockerLocationCreateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        validatorBucket
                .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)));


        LockerLocationDomainModel lockerLocationDomainModel = LockerLocationDomainModel.of(
                lockerLocationCreateRequestDto.getName(),
                lockerLocationCreateRequestDto.getDescription()
        );

        this.lockerLocationPort.findByName(lockerLocationDomainModel.getName()).ifPresent(
                name -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            "Duplicated locker location name"
                    );
                }
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(lockerLocationDomainModel, this.validator))
                .validate();

        return LockerLocationResponseDto.from(
                this.lockerLocationPort.create(lockerLocationDomainModel),
                0L,
                0L
        );
    }

    @Transactional(readOnly = false)
    public LockerLocationResponseDto updateLocation(
            String updaterId,
            String locationId,
            LockerLocationUpdateRequestDto lockerLocationRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        validatorBucket
                .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)));

        LockerLocationDomainModel lockerLocation = this.lockerLocationPort.findById(locationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker location id"
                )
        );

        if (!lockerLocation.getName().equals(lockerLocationRequestDto.getName())) {
            this.lockerLocationPort.findByName(lockerLocationRequestDto.getName()).ifPresent(
                    name -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                "Duplicated locker location name"
                        );
                    }
            );
        }

        LockerLocationDomainModel lockerLocationDomainModel = LockerLocationDomainModel.of(
                lockerLocation.getId(),
                lockerLocationRequestDto.getName(),
                lockerLocationRequestDto.getDescription()
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(lockerLocationDomainModel, this.validator))
                .validate();

        return LockerLocationResponseDto.from(
                this.lockerLocationPort.update(locationId, lockerLocationDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Locker location id checked, but exception occurred"
                        )
                ),
                this.lockerPort.getEnableLockerCountByLocation(lockerLocationDomainModel.getId()),
                this.lockerPort.getLockerCountByLocation(lockerLocationDomainModel.getId())
        );
    }

    @Transactional(readOnly = true)
    public List<LockerLogDetailDto> findLog(String id) {
        LockerDomainModel locker = this.lockerPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        );

        return this.lockerLogPort.findByLockerNumber(locker.getLockerNumber());
    }
}
