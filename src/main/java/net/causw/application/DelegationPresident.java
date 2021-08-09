package net.causw.application;

import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.Role;

import java.util.List;

public class DelegationPresident implements Delegation {

    private final UserPort userPort;

    public DelegationPresident(UserPort userPort) {
        this.userPort = userPort;
    }

    @Override
    public void delegate(String currentId, String targetId) {
        List<UserFullDto> councilList = this.userPort.findByRole(Role.COUNCIL);
        if (councilList != null) {
            councilList.forEach(
                    user -> this.userPort.updateRole(user.getId(), Role.COMMON)
            );
        }

        this.userPort.updateRole(currentId, Role.COMMON).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid login user id"
                )
        );
    }
}
