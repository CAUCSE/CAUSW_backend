package net.causw.domain.validation;

import net.causw.application.spi.UserCirclePort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserCircleStatus;

public class UserCircleStateLeaveValidator extends AbstractValidator {

    private final UserCirclePort userCirclePort;
    private final String userId;
    private final String circleId;

    private UserCircleStateLeaveValidator(
            UserCirclePort userCirclePort,
            String userId,
            String circleId
    ) {
        this.userCirclePort = userCirclePort;
        this.userId = userId;
        this.circleId = circleId;
    }

    public static UserCircleStateLeaveValidator of(
            UserCirclePort userCirclePort,
            String userId,
            String circleId
    ) {
        return new UserCircleStateLeaveValidator(
                userCirclePort,
                userId,
                circleId
        );
    }

    @Override
    public void validate() {
        this.userCirclePort.loadUserCircleStatus(this.userId, this.circleId).ifPresent(
                status -> {
                    if (status == UserCircleStatus.MEMBER) {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                "The user is already member of the circle"
                        );
                    }

                    if (status == UserCircleStatus.AWAIT) {
                        throw new UnauthorizedException(
                                ErrorCode.AWAITING_USER,
                                "The user is awaiting the approval from the circle"
                        );
                    }

                    if (status == UserCircleStatus.DROP) {
                        throw new UnauthorizedException(
                                ErrorCode.BLOCKED_USER,
                                "The user is blocked from the circle"
                        );
                    }
                }
        );

        if (this.hasNext()) {
            this.next.validate();
        }
    }
}
