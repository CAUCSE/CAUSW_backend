package net.causw.application.dto.exception;

import lombok.Getter;
import net.causw.domain.exceptions.ErrorCode;

import java.time.LocalDateTime;

@Getter
public class ExceptionDto {
    private final Integer errorCode;
    private final String message;
    private final LocalDateTime timeStamp;

    public ExceptionDto(ErrorCode errorCode, String message) {
        this.errorCode = errorCode.getCode();
        this.message = message;
        this.timeStamp = LocalDateTime.now();
    }
}
