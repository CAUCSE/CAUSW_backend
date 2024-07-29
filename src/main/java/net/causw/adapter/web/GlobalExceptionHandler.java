package net.causw.adapter.web;

import lombok.extern.slf4j.Slf4j;
import net.causw.application.dto.exception.ConstraintExceptionDto;
import net.causw.application.dto.exception.ExceptionDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.exceptions.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;

@Component
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadRequestException(BadRequestException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ConstraintExceptionDto handleConstraintViolationException(ConstraintViolationException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ConstraintExceptionDto.of(ErrorCode.INVALID_PARAMETER, exception.getMessage(), exception);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleIllegalArgumentException(IllegalArgumentException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(ErrorCode.INVALID_PARAMETER, exception.getMessage());
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadHttpRequestMethodException(HttpRequestMethodNotSupportedException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(ErrorCode.INVALID_HTTP_METHOD, "Invalid request http method (GET, POST, PUT, DELETE)");
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(ErrorCode.VALIDATE_FAILURE, exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleUnauthorizedException(UnauthorizedException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ServiceUnavailableException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionDto handleServiceUnavailableException(ServiceUnavailableException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleAccessDeniedException(AccessDeniedException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(ErrorCode.API_NOT_ACCESSIBLE, exception.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto unknownException(Exception exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return ExceptionDto.of(ErrorCode.INTERNAL_SERVER, "Internal server error");
    }
}
