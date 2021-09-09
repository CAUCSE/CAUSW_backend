package net.causw.domain.validation;

import net.causw.application.dto.CircleFullDto;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;

public class CorrectCircleLeaderValidator extends AbstractValidator {

    private final CircleFullDto circleFullDto;
    private final String leaderId;

    private CorrectCircleLeaderValidator(CircleFullDto circleFullDto, String leaderId) {
        this.circleFullDto = circleFullDto;
        this.leaderId = leaderId;
    }

    public static CorrectCircleLeaderValidator of(CircleFullDto circleFullDto, String leaderId) {
        return new CorrectCircleLeaderValidator(circleFullDto, leaderId);
    }

    @Override
    public void validate() {
        if (this.circleFullDto != null) {
            if (this.circleFullDto.getManager().getId().equals(this.leaderId)) {
                throw new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        "You don't have auth"
                );
            }
        }

        if (this.hasNext()) {
            this.next.validate();
        }
    }
}
