package net.causw.application;

import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ValidatorBucket;

import java.util.List;

/**
 * The delegation process for the leader of the circle.
 * The leader of the circle entity is updated.
 * The user who is leader become COMMON state in this process.
 */
public class DelegationLeaderCircle implements Delegation {

    private final UserPort userPort;

    private final CirclePort circlePort;

    private final CircleMemberPort circleMemberPort;

    public DelegationLeaderCircle(
            UserPort userPort,
            CirclePort circlePort,
            CircleMemberPort circleMemberPort
    ) {
        this.userPort = userPort;
        this.circlePort = circlePort;
        this.circleMemberPort = circleMemberPort;
    }

    @Override
    public void delegate(String currentId, String targetId) {
        CircleDomainModel circle = this.circlePort.findByLeaderId(currentId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        "Invalid leader id"
                )
        );

        UserDomainModel newLeader = this.userPort.findById(targetId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(targetId, circle.getId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid application id"
                )
        );

        ValidatorBucket.of()
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .validate();

        this.circlePort.updateLeader(circle.getId(), newLeader).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Circle id and Leader id checked, but exception occurred"
                )
        );

        this.userPort.updateRole(currentId, Role.COMMON).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        );
    }
}
