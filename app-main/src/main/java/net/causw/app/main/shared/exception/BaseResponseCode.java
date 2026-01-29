package net.causw.app.main.shared.exception;

import org.springframework.http.HttpStatus;

public interface BaseResponseCode {

	String getCode();

	String getMessage();

	HttpStatus getStatus();
}
