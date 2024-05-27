package net.causw.domain.validation;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.StaticValue;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimePassedValidator extends AbstractValidator {
    private final LocalDateTime updatedAt;

    private TimePassedValidator(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static TimePassedValidator of(LocalDateTime updatedAt) {
        return new TimePassedValidator(updatedAt);
    }

    @Override
    public void validate() {
        Duration duration = Duration.between(this.updatedAt, LocalDateTime.now());

        if (duration.getSeconds() < StaticValue.JWT_ACCESS_THRESHOLD) {
            LocalDateTime allowedTime = this.updatedAt.plusSeconds(StaticValue.JWT_ACCESS_THRESHOLD);

            String message =
                            allowedTime.getYear() + "-" +
                            allowedTime.getMonthValue() + "-" +
                            allowedTime.getDayOfMonth() + " " +
                            allowedTime.getHour() + ":" +
                            allowedTime.getMinute() + ":" +
                            allowedTime.getSecond() +
                            " 이후에 다시 시도해주세요.";

            throw new BadRequestException(
                    ErrorCode.TIME_NOT_PASSED,
                    message
            );
        }
    }
}
