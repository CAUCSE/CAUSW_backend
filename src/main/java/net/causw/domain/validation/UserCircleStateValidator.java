package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserCircleStatus;

import java.util.List;

public class UserCircleStateValidator extends AbstractValidator {

    private final UserCircleStatus status;

    private final List<UserCircleStatus> targetStatusList;

    private UserCircleStateValidator(UserCircleStatus status, List<UserCircleStatus> targetStatusList) {
        this.status = status;
        this.targetStatusList = targetStatusList;
    }

    public static UserCircleStateValidator of(UserCircleStatus status, List<UserCircleStatus> targetStatusList) {
        return new UserCircleStateValidator(status, targetStatusList);
    }

    @Override
    public void validate() {
        for (UserCircleStatus targetStatus : this.targetStatusList) {
            if (this.status.equals(targetStatus)) {
                if (targetStatus == UserCircleStatus.MEMBER) {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            "The user is already member of the circle"
                    );
                }

                if (targetStatus == UserCircleStatus.LEAVE) {
                    throw new BadRequestException(
                            ErrorCode.APPLY_NOT_EXIST,
                            "The user did not applied to the circle"
                    );
                }

                if (targetStatus == UserCircleStatus.AWAIT) {
                    throw new UnauthorizedException(
                            ErrorCode.AWAITING_USER,
                            "The user is awaiting the approval from the circle"
                    );
                }

                if (targetStatus == UserCircleStatus.DROP) {
                    throw new UnauthorizedException(
                            ErrorCode.BLOCKED_USER,
                            "The user is blocked from the circle"
                    );
                }
            }
        }
    }
}
