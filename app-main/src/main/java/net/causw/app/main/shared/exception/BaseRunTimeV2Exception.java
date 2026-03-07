package net.causw.app.main.shared.exception;

import lombok.Getter;

@Getter
public class BaseRunTimeV2Exception extends RuntimeException {

	private final BaseResponseCode errorCode;

	public BaseRunTimeV2Exception(BaseResponseCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
