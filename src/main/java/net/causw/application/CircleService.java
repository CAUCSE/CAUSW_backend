package net.causw.application;

import net.causw.application.dto.CircleCreateRequestDto;
import net.causw.application.dto.CircleMemberResponseDto;
import net.causw.application.dto.CircleResponseDto;
import net.causw.application.dto.CircleUpdateRequestDto;
import net.causw.application.dto.DuplicatedCheckDto;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.GrantableRoleValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.TargetIsNullValidator;
import net.causw.domain.validation.TimePassedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserNotEqualValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CircleService {
    private final CirclePort circlePort;
    private final UserPort userPort;
    private final CircleMemberPort circleMemberPort;
    private final Validator validator;

    public CircleService(
            CirclePort circlePort,
            UserPort userPort,
            CircleMemberPort circleMemberPort,
            Validator validator
    ) {
        this.circlePort = circlePort;
        this.userPort = userPort;
        this.circleMemberPort = circleMemberPort;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public CircleResponseDto findById(String id) {
        CircleDomainModel circle = this.circlePort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted()))
                .validate();

        return CircleResponseDto.from(
                circle,
                this.circleMemberPort.getNumMember(id)
        );
    }

    @Transactional(readOnly = true)
    public Long getNumMember(String id) {
        this.circlePort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );

        return this.circleMemberPort.getNumMember(id);
    }

    @Transactional(readOnly = true)
    public List<CircleMemberResponseDto> getUserList(
            String currentUserId,
            String circleId,
            CircleMemberStatus status
    ) {
        UserDomainModel user = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.LEADER_CIRCLE, Role.PRESIDENT)))
                .validate();

        return this.circleMemberPort.findByCircleId(circleId, status)
                .stream()
                .map(CircleMemberResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CircleResponseDto create(String userId, CircleCreateRequestDto circleCreateRequestDto) {
        UserDomainModel requestUser = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        UserDomainModel leader = this.userPort.findById(circleCreateRequestDto.getLeaderId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid leader id"
                )
        );

        CircleDomainModel circleDomainModel = CircleDomainModel.of(
                circleCreateRequestDto.getName(),
                circleCreateRequestDto.getMainImage(),
                circleCreateRequestDto.getDescription(),
                leader
        );

        /* Check if the request user is president or admin
         * Then, validate the circle name whether it is duplicated or not
         */
        this.circlePort.findByName(circleDomainModel.getName()).ifPresent(
                name -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            "Duplicated circle name"
                    );
                }
        );

        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(circleDomainModel, this.validator))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(GrantableRoleValidator.of(requestUser.getRole(), Role.LEADER_CIRCLE, leader.getRole()))
                .validate();

        // Grant role to the LEADER
        leader = this.userPort.updateRole(circleCreateRequestDto.getLeaderId(), Role.LEADER_CIRCLE).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.INTERNAL_SERVER,
                        "Leader id checked, but exception occurred"
                )
        );

        // Create circle
        CircleDomainModel newCircle = this.circlePort.create(circleDomainModel);

        // Apply the leader automatically to the circle
        CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.create(leader, newCircle);
        this.circleMemberPort.updateStatus(circleMemberDomainModel.getId(), CircleMemberStatus.MEMBER).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Circle id immediately can be used, but exception occurred"
                )
        );

        return CircleResponseDto.from(newCircle);
    }

    @Transactional
    public CircleResponseDto update(
            String userId,
            String circleId,
            CircleUpdateRequestDto circleUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );

        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        if (!circle.getName().equals(circleUpdateRequestDto.getName())) {
            this.circlePort.findByName(circleUpdateRequestDto.getName()).ifPresent(
                    name -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                "Duplicated circle name"
                        );
                    }
            );
        }

        CircleDomainModel circleDomainModel = CircleDomainModel.of(
                circle.getId(),
                circleUpdateRequestDto.getName(),
                circleUpdateRequestDto.getMainImage(),
                circleUpdateRequestDto.getDescription(),
                circle.getIsDeleted(),
                circle.getLeader()
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted()))
                .consistOf(ConstraintValidator.of(circleDomainModel, this.validator))
                .consistOf(UserRoleValidator.of(
                        user.getRole(),
                        List.of(Role.PRESIDENT, Role.LEADER_CIRCLE)
                ));

        if (user.getRole().equals(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(circleDomainModel.getLeader().getId(), user.getId()));
        }

        validatorBucket
                .validate();

        return CircleResponseDto.from(this.circlePort.update(circleId, circleDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Circle id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public CircleMemberResponseDto userApply(String userId, String circleId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );

        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted()))
                .consistOf(TargetIsNullValidator.of(user.getStudentId()));

        return CircleMemberResponseDto.from(this.circleMemberPort.findByUserIdAndCircleId(user.getId(), circle.getId()).map(
                circleMember -> {
                    validatorBucket
                            .consistOf(
                                    CircleMemberStatusValidator.of(
                                            circleMember.getStatus(),
                                            List.of(CircleMemberStatus.LEAVE, CircleMemberStatus.REJECT)
                                    )
                            )
                            .consistOf(TimePassedValidator.of(circleMember.getUpdatedAt()))
                            .validate();

                    return this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.AWAIT).orElseThrow(
                            () -> new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    "Application id checked, but exception occurred"
                            )
                    );
                }
        ).orElseGet(
                () -> {
                    validatorBucket.validate();
                    return this.circleMemberPort.create(user, circle);
                }
        ));
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckDto isDuplicatedName(String name) {
        return DuplicatedCheckDto.of(this.circlePort.findByName(name).isPresent());
    }

    @Transactional
    public CircleMemberResponseDto leaveUser(String userId, String circleId) {
        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(user.getId(), circle.getId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "The user is not a member of circle"
                )
        );

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted()))
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .consistOf(UserNotEqualValidator.of(userId, circleMember.getCircle().getLeader().getId()))
                .validate();

        return CircleMemberResponseDto.from(this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public CircleMemberResponseDto dropUser(
            String requestUserId,
            String userId,
            String circleId
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "The user is not a member of circle"
                )
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.LEADER_CIRCLE)));

        if (requestUser.getRole().equals(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(requestUserId, circleMember.getCircle().getLeader().getId()));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .consistOf(UserNotEqualValidator.of(userId, circleMember.getCircle().getLeader().getId()))
                .validate();

        return CircleMemberResponseDto.from(this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.DROP).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public CircleMemberResponseDto acceptUser(String requestUserId, String applicationId) {
        return this.updateUserApplication(
                requestUserId,
                applicationId,
                CircleMemberStatus.MEMBER
        );
    }

    @Transactional
    public CircleMemberResponseDto rejectUser(String requestUserId, String applicationId) {
        return this.updateUserApplication(
                requestUserId,
                applicationId,
                CircleMemberStatus.REJECT
        );
    }

    private CircleMemberResponseDto updateUserApplication(
            String requestUserId,
            String applicationId,
            CircleMemberStatus targetStatus
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findById(applicationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid application id"
                )
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.LEADER_CIRCLE)));

        if (requestUser.getRole().equals(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(requestUserId, circleMember.getCircle().getLeader().getId()));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.AWAIT)
                ))
                .validate();

        return CircleMemberResponseDto.from(this.circleMemberPort.updateStatus(applicationId, targetStatus).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }
}
