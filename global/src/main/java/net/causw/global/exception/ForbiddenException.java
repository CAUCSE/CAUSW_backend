package net.causw.global.exception;

public class ForbiddenException extends BaseRuntimeException {
    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
