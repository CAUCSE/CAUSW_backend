package net.causw.app.main.shared.exception;

import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.BaseRuntimeException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.ForbiddenException;
import net.causw.global.exception.ServiceUnavailableException;
import net.causw.global.exception.UnauthorizedException;

import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RestControllerAdvice
public class GlobalV2ExceptionHandler {

	@ExceptionHandler(value = {BaseRunTimeV2Exception.class})
	public ResponseEntity<ApiResponse<?>> handleBaseRunTimeV2Exception(BaseRunTimeV2Exception exception) {
		BaseResponseCode errorCode = exception.getErrorCode();
		HttpStatus status = errorCode.getStatus();
		// 로깅
		log.warn("Error occurred - Code: {}, Message: {}", errorCode.getCode(), exception.getMessage());

		return ResponseEntity.status(status)
			.body(ApiResponse.error(errorCode.getCode(), exception.getMessage()));
	}

	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		log.warn("Validation failed: {}", exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
		return ApiResponse.error(GlobalErrorCode.BAD_REQUEST.getCode(),
			exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
	}

	@ExceptionHandler(value = {BadRequestException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<String> handleBadRequestException(BadRequestException exception) {
		log.warn("Bad request: {} - {}", exception.getErrorCode(), exception.getMessage());
		return ApiResponse.error(GlobalErrorCode.BAD_REQUEST);
	}

	@ExceptionHandler(value = {UnauthorizedException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiResponse<String> handleUnauthorizedException(UnauthorizedException exception) {
		log.warn("Unauthorized access: {} - {}", exception.getErrorCode(), exception.getMessage());
		return ApiResponse.error(GlobalErrorCode.UNAUTHORIZED);
	}

	@ExceptionHandler(value = {ForbiddenException.class})
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiResponse<String> handleForbiddenException(ForbiddenException exception) {
		log.warn("Access forbidden: {} - {}", exception.getErrorCode(), exception.getMessage());
		return ApiResponse.error(GlobalErrorCode.FORBIDDEN);
	}

	@ExceptionHandler(value = {ServiceUnavailableException.class})
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ApiResponse<String> handleServiceUnavailableException(ServiceUnavailableException exception) {
		log.error("Service unavailable: {} - {}", exception.getErrorCode(), exception.getMessage(), exception);
		return ApiResponse.error(GlobalErrorCode.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(value = {BaseRuntimeException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<String> handleBaseRuntimeException(BaseRuntimeException exception) {
		ErrorCode errorCode = exception.getErrorCode();

		try {
			// HTTP 상태 코드 변환
			int httpStatusCode = errorCode.getHttpStatusCode();
			HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode);

			// 로깅
			logException(httpStatus, errorCode, exception);

			return ApiResponse.error(GlobalErrorCode.BAD_REQUEST);

		} catch (IllegalArgumentException e) {
			log.error("Invalid HTTP status code {} for ErrorCode {}",
				errorCode.getHttpStatusCode(), errorCode, e);

			return ApiResponse.error(GlobalErrorCode.BAD_REQUEST.getCode(), exception.getMessage());
		}
	}

	@ExceptionHandler(value = {Exception.class})
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<String> unknownException(Exception exception) {
		log.error("Internal server error", exception);
		return ApiResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		String message = "잘못된 요청 형식입니다.";

		// 날짜 파싱 에러 체크
		if (ex.getCause() instanceof JsonMappingException jme) {
			if (jme.getCause() instanceof DateTimeParseException dtpe) {
				message = "날짜 형식이 올바르지 않습니다.";
				log.warn("Invalid date format in request: {}", dtpe.getParsedString(), dtpe);
			} else {
				log.warn("JSON mapping error: {}", jme.getMessage(), jme);
			}
		} else {
			log.warn("HTTP message not readable: {}", ex.getMessage(), ex);
		}

		return ApiResponse.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
	}

	// ErrorCode를 기반으로 HTTP 상태 코드 결정하는 헬퍼 메서드
	private void logException(HttpStatus httpStatus, ErrorCode errorCode, BaseRuntimeException exception) {
		if (httpStatus.is4xxClientError()) {
			// 4xx 에러는 클라이언트 실수이므로 WARN 레벨
			log.warn("Client error - Code: {}, Message: {}",
				errorCode, exception.getMessage());
		} else if (httpStatus.is5xxServerError()) {
			// 5xx 에러는 서버 문제이므로 ERROR 레벨 + 스택트레이스
			log.error("Server error - Code: {}, Message: {}",
				errorCode, exception.getMessage(), exception);
		} else {
			log.warn("Unexpected status code {} - Code: {}, Message: {}",
				httpStatus, errorCode, exception.getMessage());
		}
	}
}
