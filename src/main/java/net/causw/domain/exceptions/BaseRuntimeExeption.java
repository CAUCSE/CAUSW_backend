package net.causw.domain.exceptions;

import lombok.Getter;

@Getter
public abstract class BaseRuntimeExeption extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public BaseRuntimeExeption(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
