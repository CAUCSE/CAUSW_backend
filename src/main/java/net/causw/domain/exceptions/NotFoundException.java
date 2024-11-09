package net.causw.domain.exceptions;

public class NotFoundException extends BaseRuntimeException {
    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
