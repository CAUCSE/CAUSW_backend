package net.causw.domain.validation;

import net.causw.application.spi.CirclePort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

public class DuplicatedCircleNameValidator extends AbstractValidator {

    private final CirclePort circlePort;

    private final String circleName;

    private DuplicatedCircleNameValidator(CirclePort circlePort, String circleName) {
        this.circlePort = circlePort;
        this.circleName = circleName;
    }

    public static DuplicatedCircleNameValidator of(CirclePort circlePort, String circleName) {
        return new DuplicatedCircleNameValidator(circlePort, circleName);
    }

    @Override
    public void validate() {
        if (this.circlePort.findByName(this.circleName).isPresent()) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    "Duplicated circle name"
            );
        }
    }
}
