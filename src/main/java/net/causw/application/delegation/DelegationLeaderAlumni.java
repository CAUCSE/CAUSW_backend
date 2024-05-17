package net.causw.application.delegation;

import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;

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
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );
    }
}
