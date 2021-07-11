package net.causw.domain.exceptions;

public class UnauthorizedException extends BaseRuntimeExeption {
    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
