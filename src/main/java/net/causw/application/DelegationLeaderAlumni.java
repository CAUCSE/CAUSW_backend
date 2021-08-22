package net.causw.application;

import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.Role;

/**
 * The delegation process for the leader of the alumni.
 * The user who is leader become COMMON state in this process.
 */
public class DelegationLeaderAlumni implements Delegation {

    private final UserPort userPort;

    public DelegationLeaderAlumni(UserPort userPort) {
        this.userPort = userPort;
    }

    @Override
    public void delegate(String currentId, String targetId) {
        this.userPort.updateRole(currentId, Role.COMMON).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid login user id"
                )
        );
    }
}
