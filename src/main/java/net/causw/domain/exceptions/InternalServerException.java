package net.causw.domain.exceptions;

public class InternalServerException extends BaseRuntimeException {
    public InternalServerException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
