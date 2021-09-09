package net.causw.domain.validation;

import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;

public class BoardRoleValidator extends AbstractValidator {

    private final Role userRole;
    private final String circleId;

    private BoardRoleValidator(Role userRole, String circleId) {
        this.userRole = userRole;
        this.circleId = circleId;
    }

    public static BoardRoleValidator of(Role userRole, String circleId) {
        return new BoardRoleValidator(userRole, circleId);
    }

    @Override
    public void validate() {
        if (this.circleId == null && this.userRole != Role.PRESIDENT) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    "You don't have auth"
            );
        }

        if (this.circleId != null && this.userRole != Role.LEADER_CIRCLE) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    "You don't have auth"
            );
        }

        if (this.hasNext()) {
            this.next.validate();
        }
    }
}
