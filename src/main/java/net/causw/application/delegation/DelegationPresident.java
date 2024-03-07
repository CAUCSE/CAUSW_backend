package net.causw.application.delegation;

import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;

import java.util.List;

/**
 * The delegation process for the student president.
 * The users who have council role become COMMON state in this process.
 * The user who is student president become COMMON state in this process.
 */
public class DelegationPresident implements Delegation {

    private final UserPort userPort;

    public DelegationPresident(UserPort userPort) {
        this.userPort = userPort;
    }

    @Override
    public void delegate(String currentId, String targetId) {
        List<UserDomainModel> councilList = this.userPort.findByRole("COUNCIL");
        if (!councilList.isEmpty()) {
            councilList.forEach(
                    user -> this.userPort.removeRole(user.getId(), Role.COUNCIL)
            );
        }

        List<UserDomainModel> vicePresident = this.userPort.findByRole("VICE_PRESIDENT");
        if (!vicePresident.isEmpty()) {
            vicePresident.forEach(
                    user -> this.userPort.removeRole(user.getId(), Role.VICE_PRESIDENT)
            );
        }

        this.userPort.removeRole(currentId, Role.PRESIDENT).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        );
    }
}
