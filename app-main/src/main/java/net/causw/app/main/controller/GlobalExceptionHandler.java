package net.causw.app.main.controller;

import java.time.format.DateTimeParseException;

import lombok.extern.slf4j.Slf4j;
import net.causw.app.main.dto.exception.ConstraintExceptionDto;
import net.causw.app.main.dto.exception.ExceptionDto;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.BaseRuntimeException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.ForbiddenException;
import net.causw.global.exception.UnauthorizedException;
import net.causw.global.exception.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.databind.JsonMappingException;

@Component
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.warn("Validation failed: {}", exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return ExceptionDto.of(ErrorCode.VALIDATION_FAILED, exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(value = {BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadRequestException(BadRequestException exception) {
        log.warn("Bad request: {} - {}", exception.getErrorCode(), exception.getMessage());
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ConstraintExceptionDto handleConstraintViolationException(ConstraintViolationException exception) {
        log.warn("Constraint violation: {}", exception.getMessage());
        return ConstraintExceptionDto.of(ErrorCode.INVALID_PARAMETER, exception.getMessage(), exception);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleIllegalArgumentException(IllegalArgumentException exception) {
        log.warn("Invalid parameter: {}", exception.getMessage());
        return ExceptionDto.of(ErrorCode.INVALID_PARAMETER, exception.getMessage());
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadHttpRequestMethodException(HttpRequestMethodNotSupportedException exception) {
        log.warn("Unsupported HTTP method: {}", exception.getMethod());
        return ExceptionDto.of(ErrorCode.INVALID_HTTP_METHOD, "Invalid request http method (GET, POST, PUT, DELETE)");
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleUnauthorizedException(UnauthorizedException exception) {
        log.warn("Unauthorized access: {} - {}", exception.getErrorCode(), exception.getMessage());
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionDto handleForbiddenException(ForbiddenException exception) {
        log.warn("Access forbidden: {} - {}", exception.getErrorCode(), exception.getMessage());
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {java.nio.file.AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleFileAccessDeniedException(java.nio.file.AccessDeniedException exception) {
        log.warn("File access denied: {}", exception.getMessage());
        return ExceptionDto.of(ErrorCode.API_NOT_ACCESSIBLE, exception.getMessage());
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleAccessDeniedException(AccessDeniedException exception) {
        log.warn("Spring Security access denied: {}", exception.getMessage());
        return ExceptionDto.of(ErrorCode.API_NOT_ACCESSIBLE, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
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

        return ExceptionDto.of(ErrorCode.INVALID_PARAMETER, message);
    }

    // 500 에러

    @ExceptionHandler(value = {ServiceUnavailableException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionDto handleServiceUnavailableException(ServiceUnavailableException exception) {
        log.error("Service unavailable: {} - {}", exception.getErrorCode(), exception.getMessage(), exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {BaseRuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 기본적으로 500으로 설정
    public ExceptionDto handleBaseRuntimeException(BaseRuntimeException exception) {
        HttpStatus status = determineHttpStatus(exception.getErrorCode());
        if (status.is4xxClientError()) {
            log.warn("Client error - {}: {}", exception.getErrorCode(), exception.getMessage());
        } else {
            log.error("Server error - {}: {}", exception.getErrorCode(), exception.getMessage(), exception);
        }

        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto unknownException(Exception exception) {
        log.error("Internal server error", exception);

        return ExceptionDto.of(ErrorCode.INTERNAL_SERVER, "Internal server error");
    }

    // ErrorCode를 기반으로 HTTP 상태 코드 결정하는 헬퍼 메서드
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        int code = errorCode.getCode();

        if (code >= 4000 && code < 4100) {
            return HttpStatus.BAD_REQUEST;
        } else if (code >= 4100 && code < 4200) {
            return HttpStatus.UNAUTHORIZED;
        } else if (code >= 4200 && code < 4300) {
            return HttpStatus.FORBIDDEN;
        } else if (code >= 5000 && code < 5030) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (code >= 5030 && code < 5100) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
