package net.causw.application;

import net.causw.application.dto.CircleCreateRequestDto;
import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.CircleResponseDto;
import net.causw.application.dto.DuplicatedCheckDto;
import net.causw.application.dto.UserCircleDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserCirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserCircleStatus;
import net.causw.domain.validation.CircleStateValidator;
import net.causw.domain.validation.DuplicatedCircleNameValidator;
import net.causw.domain.validation.UpdatableGranteeRoleValidator;
import net.causw.domain.validation.UserCircleStateAwaitValidator;
import net.causw.domain.validation.UserCircleStateLeaveValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleAdminOrPresidentValidator;
import net.causw.domain.validation.UserStateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CircleService {
    private final CirclePort circlePort;
    private final UserPort userPort;
    private final UserCirclePort userCirclePort;

    public CircleService(
            CirclePort circlePort,
            UserPort userPort,
            UserCirclePort userCirclePort
    ) {
        this.circlePort = circlePort;
        this.userPort = userPort;
        this.userCirclePort = userCirclePort;
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
        UserRoleAdminOrPresidentValidator.of(requestUser.getRole())
                .linkWith(DuplicatedCircleNameValidator.of(this.circlePort, circleCreateRequestDto.getName())
                        .linkWith(UpdatableGranteeRoleValidator.of(Role.LEADER_CIRCLE, leader.getRole())
                                .linkWith(UserStateValidator.of(leader.getState()))))
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
        UserCircleDto userCircleDto = this.userCirclePort.create(leader, newCircle);
        this.userCirclePort.updateStatus(userCircleDto.getId(), UserCircleStatus.MEMBER).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id immediately used, but exception occurred"
                )
        );

        return CircleResponseDto.from(newCircle);
    }

    @Transactional
    public UserCircleDto userApply(String userId, String circleId) {
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

        CircleStateValidator.of(circle.getIsDeleted())
                .linkWith(UserCircleStateLeaveValidator.of(this.userCirclePort, user.getId(), circle.getId()))
                .validate();

        return this.userCirclePort.create(user, circle);
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckDto isDuplicatedName(String name) {
        return DuplicatedCheckDto.of(this.circlePort.findByName(name).isPresent());
    }

    @Transactional
    public UserCircleDto acceptUser(String requestUserId, String applicationId) {
        return this.updateStatus(
                requestUserId,
                applicationId,
                UserCircleStatus.MEMBER
        );
    }

    @Transactional
    public UserCircleDto rejectUser(String requestUserId, String applicationId) {
        return this.updateStatus(
                requestUserId,
                applicationId,
                UserCircleStatus.REJECT
        );
    }

    private UserCircleDto updateStatus(
            String requestUserId,
            String applicationId,
            UserCircleStatus targetStatus
    ) {
        UserCircleDto userCircleDto = this.userCirclePort.findById(applicationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid application id"
                )
        );

        // TODO : Request User가 ADMIN인 경우 허용
        UserEqualValidator.of(requestUserId, userCircleDto.getCircle().getManager().getId())
                .linkWith(CircleStateValidator.of(userCircleDto.getCircle().getIsDeleted())
                        .linkWith(UserCircleStateAwaitValidator.of(userCircleDto.getStatus())))
                .validate();

        return this.userCirclePort.updateStatus(applicationId, targetStatus).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        );
    }
}
