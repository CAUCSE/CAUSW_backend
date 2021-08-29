package net.causw.adapter.web;

import lombok.extern.slf4j.Slf4j;
import net.causw.application.dto.ConstraintExceptionDto;
import net.causw.application.dto.ExceptionDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;

@Component
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadRequestException(BadRequestException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ConstraintExceptionDto handleConstraintViolationException(ConstraintViolationException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ConstraintExceptionDto(ErrorCode.INVALID_PARAMETER, exception.getMessage(), exception);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleIllegalArgumentException(IllegalArgumentException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(ErrorCode.INVALID_PARAMETER, exception.getMessage());
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleUnauthorizedException(UnauthorizedException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleAccessDeniedException(AccessDeniedException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(ErrorCode.API_NOT_ACCESSIBLE, exception.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto unknownException(Exception exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(ErrorCode.INTERNAL_SERVER, "Internal server error");
    }
}
