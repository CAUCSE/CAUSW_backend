package net.causw.domain.exceptions;

public class BadRequestException extends BaseRuntimeExeption {
    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
