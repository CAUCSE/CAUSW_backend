package net.causw.application.dto;

import lombok.Getter;
import net.causw.domain.exceptions.ErrorCode;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class ConstraintExceptionDto {
    private final Integer errorCode;
    private final String message;
    private final LocalDateTime timeStamp;
    private final Set<?> violations;

    public ConstraintExceptionDto(ErrorCode errorCode, String message, Set<?> violations) {
        this.errorCode = errorCode.getCode();
        this.message = message;
        this.violations = violations;
        this.timeStamp = LocalDateTime.now();
    }
}
