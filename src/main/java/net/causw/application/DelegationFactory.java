package net.causw.application;

import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

public class DelegationFactory {
    public static Delegation create(
            Role role,
            UserPort userPort,
            CirclePort circlePort,
            CircleMemberPort circleMemberPort
    ) {
        switch (role) {
            case PRESIDENT:
                return new DelegationPresident(userPort);
            case LEADER_CIRCLE:
                return new DelegationLeaderCircle(userPort, circlePort, circleMemberPort);
            case LEADER_ALUMNI:
                return new DelegationLeaderAlumni(userPort);
            default:
                throw new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        "위임할 수 있는 권한이 아닙니다."
                );
        }
    }
}