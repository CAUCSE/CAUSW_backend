package net.causw.domain.validation;

import net.causw.domain.model.Role;

public class CircleLeaderValidator extends AbstractValidator {
    private final String requestUserId;
    private final String leaderId;

    private final Role requestUserRole;

    private CircleLeaderValidator(
            String requestUserId,
            String leaderId,
            Role requestUserRole
    ) {
        this.requestUserId = requestUserId;
        this.leaderId = leaderId;
        this.requestUserRole = requestUserRole;
    }

    public static CircleLeaderValidator of(
            String requestUserId,
            String leaderId,
            Role requestUserRole
    ) {
        return new CircleLeaderValidator(
                requestUserId,
                leaderId,
                requestUserRole
        );
    }


    @Override
    public void validate() {
        if (requestUserRole.equals(Role.LEADER_CIRCLE)) {
            UserEqualValidator.of(
                    leaderId,
                    requestUserId
            ).validate();
        }
    }
}
