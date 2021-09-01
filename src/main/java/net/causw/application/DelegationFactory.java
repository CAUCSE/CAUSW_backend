package net.causw.application;

import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

public class DelegationFactory {
    public static Delegation create(Role role, UserPort userPort, CirclePort circlePort) {
        switch (role) {
            case PRESIDENT:
                return new DelegationPresident(userPort);
            case LEADER_CIRCLE:
                return new DelegationLeaderCircle(userPort, circlePort);
            case LEADER_ALUMNI:
                return new DelegationLeaderAlumni(userPort);
            default:
                throw new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        "You don't have access."
                );
        }
    }
}