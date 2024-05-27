package net.causw.domain.exceptions;

public class BadRequestException extends BaseRuntimeException {
    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
