package net.causw.domain.exceptions;

public class UnauthorizedException extends BaseRuntimeException {
    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
