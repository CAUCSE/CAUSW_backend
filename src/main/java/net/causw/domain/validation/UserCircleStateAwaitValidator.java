package net.causw.domain.validation;

import net.causw.application.spi.UserCirclePort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserCircleStatus;

public class UserCircleStateAwaitValidator extends AbstractValidator {

    private UserCircleStatus status;
    private UserCirclePort userCirclePort;
    private String userId;
    private String circleId;

    private UserCircleStateAwaitValidator(
            UserCirclePort userCirclePort,
            String userId,
            String circleId
    ) {
        this.userCirclePort = userCirclePort;
        this.userId = userId;
        this.circleId = circleId;
        this.status = null;
    }

    private UserCircleStateAwaitValidator(UserCircleStatus status) {
        this.status = status;
    }

    public static UserCircleStateAwaitValidator of(
            UserCirclePort userCirclePort,
            String userId,
            String circleId
    ) {
        return new UserCircleStateAwaitValidator(
                userCirclePort,
                userId,
                circleId
        );
    }

    public static UserCircleStateAwaitValidator of(UserCircleStatus status) {
        return new UserCircleStateAwaitValidator(status);
    }

    @Override
    public void validate() {
        if (this.status == null) {
            this.status = this.userCirclePort.loadUserCircleStatus(this.userId, this.circleId).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.ROW_DOES_NOT_EXIST,
                            "Invalid application information"
                    )
            );
        }

        if (this.status == UserCircleStatus.MEMBER) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    "The user is already member of the circle"
            );
        }

        if (this.status == UserCircleStatus.LEAVE) {
            throw new BadRequestException(
                    ErrorCode.APPLY_NOT_EXIST,
                    "The user did not applied to the circle"
            );
        }

        if (this.status == UserCircleStatus.DROP) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "The user is blocked from the circle"
            );
        }

        if (this.hasNext()) {
            this.next.validate();
        }
    }
}
