package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.CircleMemberStatus;

import java.util.List;

public class CircleMemberStatusValidator extends AbstractValidator {

    private final CircleMemberStatus status;

    private final List<CircleMemberStatus> statusList;

    private CircleMemberStatusValidator(CircleMemberStatus status, List<CircleMemberStatus> statusList) {
        this.status = status;
        this.statusList = statusList;
    }

    public static CircleMemberStatusValidator of(CircleMemberStatus status, List<CircleMemberStatus> statusList) {
        return new CircleMemberStatusValidator(status, statusList);
    }

    @Override
    public void validate() {
        for (CircleMemberStatus allowedStatus : this.statusList) {
            if (this.status.equals(allowedStatus)) {
                return;
            }
        }

        switch (this.status) {
            case MEMBER:
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        "The user is already member of the circle"
                );
            case LEAVE:
                throw new BadRequestException(
                        ErrorCode.APPLY_NOT_EXIST,
                        "The user did not applied to the circle"
                );
            case AWAIT:
                throw new BadRequestException(
                        ErrorCode.AWAITING_STATUS,
                        "The user is awaiting the approval from the circle"
                );
            case REJECT:
                throw new UnauthorizedException(
                        ErrorCode.BLOCKED_USER,
                        "The user is rejected from the circle"
                );
            case DROP:
                throw new UnauthorizedException(
                        ErrorCode.BLOCKED_USER,
                        "The user is blocked from the circle"
                );
            default:
                break;
        }
    }
}