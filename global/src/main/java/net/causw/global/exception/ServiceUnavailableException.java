package net.causw.global.exception;

public class ServiceUnavailableException extends BaseRuntimeException {
	public ServiceUnavailableException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
