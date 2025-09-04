package net.causw.app.main.dto.exception;

import java.time.LocalDateTime;

import net.causw.global.exception.ErrorCode;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExceptionDto {
	private final Integer errorCode;
	private final String message;
	private final LocalDateTime timeStamp;

	public static ExceptionDto of(ErrorCode errorCode, String message) {
		return ExceptionDto.builder()
			.errorCode(errorCode.getCode())
			.message(message)
			.timeStamp(LocalDateTime.now())
			.build();
	}
}
