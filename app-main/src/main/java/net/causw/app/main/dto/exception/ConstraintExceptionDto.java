package net.causw.app.main.dto.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.global.exception.ErrorCode;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class ConstraintExceptionDto {
    private final Integer errorCode;
    private final String message;
    private final LocalDateTime timeStamp;
    private final List<String> violations;

    public static ConstraintExceptionDto of(ErrorCode errorCode, String message, ConstraintViolationException exception) {
        List<String> errors = new ArrayList<>();
        exception.getConstraintViolations().forEach(violation ->
                errors.add(violation.getRootBeanClass().getName() + " " +
                        violation.getPropertyPath() + ": " + violation.getMessage()));

        return ConstraintExceptionDto.builder()
                .errorCode(errorCode.getCode())
                .message(message)
                .violations(errors)
                .timeStamp(LocalDateTime.now())
                .build();
    }
}
