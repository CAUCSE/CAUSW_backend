package net.causw.application.dto.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.exceptions.ErrorCode;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ExceptionDto {
    private final Integer errorCode;
    private final String message;
    private final LocalDateTime timeStamp;

    public static ExceptionDto of(ErrorCode errorCode, String message) {
        return ExceptionDto.builder()
                .errorCode(errorCode.getCode())
                .message(message)
                .timeStamp(LocalDateTime.now())
                .build();
    }
}
