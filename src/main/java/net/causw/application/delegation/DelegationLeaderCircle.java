package net.causw.application.delegation;

import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
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
    private final String circleId;

    public DelegationLeaderCircle(
            UserPort userPort,
            CirclePort circlePort,
            CircleMemberPort circleMemberPort,
            String circleId
    ) {
        this.userPort = userPort;
        this.circlePort = circlePort;
        this.circleMemberPort = circleMemberPort;
        this.circleId = circleId;
    }

    @Override
    public void delegate(String currentId, String targetId) {
        CircleDomainModel circle = this.circlePort.findById(this.circleId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        "권한을 위임할 소모임장의 소모임을 찾을 수 없습니다."
                )
        );

        UserDomainModel newLeader = this.userPort.findById(targetId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "피위임자를 찾을 수 없습니다."
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(targetId, circle.getId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "피위임자가 가입 신청한 소모임이 아닙니다."
                )
        );

        boolean isCircleLeader = circle.getLeader().map(UserDomainModel::getId).orElse("").equals(currentId);

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

        List<CircleDomainModel> ownCircles = this.circlePort.findByLeaderId(currentId);
        if(isCircleLeader && ownCircles.size() == 1){
            this.userPort.removeRole(currentId, Role.LEADER_CIRCLE).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            "User id checked, but exception occurred"
                    )
            );
        }

    }
}
