package net.causw.application;

import net.causw.application.dto.CircleCreateRequestDto;
import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.CircleMemberDto;
import net.causw.application.dto.CircleResponseDto;
import net.causw.application.dto.DuplicatedCheckDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.Role;
import net.causw.domain.validation.CircleMemberInvalidStatusValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.DuplicatedCircleNameValidator;
import net.causw.domain.validation.UpdatableGranteeRoleValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserNotEqualValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CircleService {
    private final CirclePort circlePort;
    private final UserPort userPort;
    private final CircleMemberPort circleMemberPort;

    public CircleService(
            CirclePort circlePort,
            UserPort userPort,
            CircleMemberPort circleMemberPort
    ) {
        this.circlePort = circlePort;
        this.userPort = userPort;
        this.circleMemberPort = circleMemberPort;
    }

    @Transactional(readOnly = true)
    public CircleResponseDto findById(String id) {
        return CircleResponseDto.from(this.circlePort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        ));
    }

    @Transactional
    public CircleResponseDto create(String userId, CircleCreateRequestDto circleCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserFullDto requestUser = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        UserFullDto leader = this.userPort.findById(circleCreateRequestDto.getLeaderId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid leader id"
                )
        );

        /* Check if the request user is president or admin
         * Then, validate the circle name whether it is duplicated or not
         */
        validatorBucket
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT, Role.ADMIN)))
                .consistOf(DuplicatedCircleNameValidator.of(this.circlePort, circleCreateRequestDto.getName()))
                .consistOf(UpdatableGranteeRoleValidator.of(Role.LEADER_CIRCLE, leader.getRole()))
                .consistOf(UserStateValidator.of(leader.getState()))
                .validate();

        // Grant role to the LEADER
        leader = this.userPort.updateRole(circleCreateRequestDto.getLeaderId(), Role.LEADER_CIRCLE).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid leader id"
                )
        );

        // Create circle
        CircleFullDto newCircle = this.circlePort.create(circleCreateRequestDto, leader);

        // Apply the leader automatically to the circle
        CircleMemberDto circleMemberDto = this.circleMemberPort.create(leader, newCircle);
        this.circleMemberPort.updateStatus(circleMemberDto.getId(), CircleMemberStatus.MEMBER).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id immediately used, but exception occurred"
                )
        );

        return CircleResponseDto.from(newCircle);
    }

    @Transactional
    public CircleMemberDto userApply(String userId, String circleId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        CircleFullDto circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid circle id"
                )
        );

        UserFullDto user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        validatorBucket.consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted()));

        Optional<CircleMemberDto> circleMemberDto = this.circleMemberPort.findByUserIdAndCircleId(user.getId(), circle.getId());
        if (circleMemberDto.isPresent()) {
            validatorBucket
                    .consistOf(
                            CircleMemberInvalidStatusValidator.of(
                                    circleMemberDto.get().getStatus(),
                                    List.of(CircleMemberStatus.MEMBER, CircleMemberStatus.DROP, CircleMemberStatus.AWAIT)
                            )
                    )
                    .validate();

            return this.circleMemberPort.updateStatus(circleMemberDto.get().getId(), CircleMemberStatus.AWAIT).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            "Application id checked, but exception occurred"
                    )
            );
        }

        validatorBucket.validate();
        return this.circleMemberPort.create(user, circle);
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckDto isDuplicatedName(String name) {
        return DuplicatedCheckDto.of(this.circlePort.findByName(name).isPresent());
    }

    @Transactional
    public CircleMemberDto leaveUser(String userId, String circleId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        CircleMemberDto circleMemberDto = this.circleMemberPort.findByUserIdAndCircleId(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid application information"
                )
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circleMemberDto.getCircle().getIsDeleted()))
                .consistOf(CircleMemberInvalidStatusValidator.of(
                        circleMemberDto.getStatus(),
                        List.of(CircleMemberStatus.AWAIT, CircleMemberStatus.DROP, CircleMemberStatus.LEAVE)
                ))
                .consistOf(UserNotEqualValidator.of(userId, circleMemberDto.getCircle().getManager().getId()))
                .validate();

        return this.circleMemberPort.updateStatus(circleMemberDto.getId(), CircleMemberStatus.LEAVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        );
    }

    @Transactional
    public CircleMemberDto dropUser(
            String requestUserId,
            String userId,
            String circleId
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        CircleMemberDto circleMemberDto = this.circleMemberPort.findByUserIdAndCircleId(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid application information"
                )
        );

        // TODO : Request User가 ADMIN인 경우 허용
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circleMemberDto.getCircle().getIsDeleted()))
                .consistOf(UserEqualValidator.of(requestUserId, circleMemberDto.getCircle().getManager().getId()))
                .consistOf(CircleMemberInvalidStatusValidator.of(
                        circleMemberDto.getStatus(),
                        List.of(CircleMemberStatus.AWAIT, CircleMemberStatus.DROP, CircleMemberStatus.LEAVE)
                ))
                .consistOf(UserNotEqualValidator.of(userId, circleMemberDto.getCircle().getManager().getId()))
                .validate();

        return this.circleMemberPort.updateStatus(circleMemberDto.getId(), CircleMemberStatus.DROP).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        );
    }

    @Transactional
    public CircleMemberDto acceptUser(String requestUserId, String applicationId) {
        return this.updateUserApplication(
                requestUserId,
                applicationId,
                CircleMemberStatus.MEMBER
        );
    }

    @Transactional
    public CircleMemberDto rejectUser(String requestUserId, String applicationId) {
        return this.updateUserApplication(
                requestUserId,
                applicationId,
                CircleMemberStatus.REJECT
        );
    }

    private CircleMemberDto updateUserApplication(
            String requestUserId,
            String applicationId,
            CircleMemberStatus targetStatus
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        CircleMemberDto circleMemberDto = this.circleMemberPort.findById(applicationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid application id"
                )
        );

        // TODO : Request User가 ADMIN인 경우 허용
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circleMemberDto.getCircle().getIsDeleted()))
                .consistOf(UserEqualValidator.of(requestUserId, circleMemberDto.getCircle().getManager().getId()))
                .consistOf(CircleMemberInvalidStatusValidator.of(
                        circleMemberDto.getStatus(),
                        List.of(CircleMemberStatus.MEMBER, CircleMemberStatus.DROP, CircleMemberStatus.LEAVE)
                ))
                .validate();

        return this.circleMemberPort.updateStatus(applicationId, targetStatus).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        );
    }
}
