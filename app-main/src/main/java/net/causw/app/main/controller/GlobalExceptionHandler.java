package net.causw.app.main.controller;

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
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import org.springframework.security.access.AccessDeniedException;

@Component
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        GlobalExceptionHandler.log.error("Validation failed for method argument", exception);
        return ExceptionDto.of(ErrorCode.VALIDATION_FAILED, exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(value = {BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadRequestException(BadRequestException exception) {
        GlobalExceptionHandler.log.error("Bad request exception occurred", exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ConstraintExceptionDto handleConstraintViolationException(ConstraintViolationException exception) {
        GlobalExceptionHandler.log.error("Constraint violation occurred", exception);
        return ConstraintExceptionDto.of(ErrorCode.INVALID_PARAMETER, exception.getMessage(), exception);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleIllegalArgumentException(IllegalArgumentException exception) {
        GlobalExceptionHandler.log.error("Illegal argument provided", exception);
        return ExceptionDto.of(ErrorCode.INVALID_PARAMETER, exception.getMessage());
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadHttpRequestMethodException(HttpRequestMethodNotSupportedException exception) {
        GlobalExceptionHandler.log.error("Unsupported HTTP method: {}", exception.getMethod(), exception);
        return ExceptionDto.of(ErrorCode.INVALID_HTTP_METHOD, "Invalid request http method (GET, POST, PUT, DELETE)");
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleUnauthorizedException(UnauthorizedException exception) {
        GlobalExceptionHandler.log.error("Unauthorized access attempt", exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionDto handleForbiddenException(ForbiddenException exception) {
        GlobalExceptionHandler.log.error("Access forbidden", exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ServiceUnavailableException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionDto handleServiceUnavailableException(ServiceUnavailableException exception) {
        GlobalExceptionHandler.log.error("Service unavailable", exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {java.nio.file.AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleFileAccessDeniedException(java.nio.file.AccessDeniedException exception) {
        GlobalExceptionHandler.log.error("File access denied", exception);
        return ExceptionDto.of(ErrorCode.API_NOT_ACCESSIBLE, exception.getMessage());
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleAccessDeniedException(AccessDeniedException exception) {
        GlobalExceptionHandler.log.error("Spring Security access denied", exception);
        return ExceptionDto.of(ErrorCode.API_NOT_ACCESSIBLE, exception.getMessage());
    }

    @ExceptionHandler(value = {BaseRuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 기본적으로 500으로 설정
    public ExceptionDto handleBaseRuntimeException(BaseRuntimeException exception) {
        GlobalExceptionHandler.log.error("Unhandled BaseRuntimeException", exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto unknownException(Exception exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(ErrorCode.INTERNAL_SERVER, "Internal server error");
    }
}
