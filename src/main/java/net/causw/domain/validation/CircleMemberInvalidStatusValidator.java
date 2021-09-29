package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.CircleMemberStatus;

import java.util.List;

public class CircleMemberInvalidStatusValidator extends AbstractValidator {

    private final CircleMemberStatus status;

    private final List<CircleMemberStatus> invalidStatusList;

    private CircleMemberInvalidStatusValidator(CircleMemberStatus status, List<CircleMemberStatus> invalidStatusList) {
        this.status = status;
        this.invalidStatusList = invalidStatusList;
    }

    public static CircleMemberInvalidStatusValidator of(CircleMemberStatus status, List<CircleMemberStatus> invalidStatusList) {
        return new CircleMemberInvalidStatusValidator(status, invalidStatusList);
    }

    @Override
    public void validate() {
        this.invalidStatusList.forEach(
            invalidStatus -> {
                if (this.status.equals(invalidStatus)) {
                    if (invalidStatus == CircleMemberStatus.MEMBER) {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                "The user is already member of the circle"
                        );
                    }

                    if (invalidStatus == CircleMemberStatus.LEAVE) {
                        throw new BadRequestException(
                                ErrorCode.APPLY_NOT_EXIST,
                                "The user did not applied to the circle"
                        );
                    }

                    if (invalidStatus == CircleMemberStatus.AWAIT) {
                        throw new BadRequestException(
                                ErrorCode.AWAITING_STATUS,
                                "The user is awaiting the approval from the circle"
                        );
                    }

                    if (invalidStatus == CircleMemberStatus.REJECT) {
                        throw new UnauthorizedException(
                                ErrorCode.BLOCKED_USER,
                                "The user is rejected from the circle"
                        );
                    }

                    if (invalidStatus == CircleMemberStatus.DROP) {
                        throw new UnauthorizedException(
                                ErrorCode.BLOCKED_USER,
                                "The user is blocked from the circle"
                        );
                    }
                }
            }
        );
    }
}
