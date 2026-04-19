package com.event.api;

import com.event.common.exception.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.OffsetDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return getErrorResponseAndLogging(BAD_REQUEST, "Validate error", e);
    }

    @ExceptionHandler({IllegalArgumentException.class, ValidationException.class, MissingRequestHeaderException.class})
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(Exception e) {
        return getErrorResponseAndLogging(BAD_REQUEST, "Client error", e);
    }

    @ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(Exception e) {
        return getErrorResponseAndLogging(NOT_FOUND, "Resource not found", e);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(UNAUTHORIZED)
    public ErrorResponse handleBadCredentialsException(Exception e) {
        return getErrorResponseAndLogging(UNAUTHORIZED, "Authentication failed", e);
    }

    @ExceptionHandler({IllegalStateException.class})
    @ResponseStatus(FORBIDDEN)
    public ErrorResponse handleIllegalStateException(IllegalStateException e) {
        return getErrorResponseAndLogging(FORBIDDEN, "Incorrect condition", e);
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException e) {
        return getErrorResponseAndLogging(FORBIDDEN, "Insufficient permissions to perform", e);
    }

    @ExceptionHandler({HttpClientErrorException.class})
    @ResponseStatus(TOO_MANY_REQUESTS)
    public ErrorResponse handleHttpClientErrorException(HttpClientErrorException e) {
        return getErrorResponseAndLogging(TOO_MANY_REQUESTS, "Too many requests", e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        return getErrorResponseAndLogging(INTERNAL_SERVER_ERROR, "Server error", e);
    }

    private static ErrorResponse getErrorResponseAndLogging(HttpStatus httpStatus, String message, Exception e) {
        log.error("Received the status {}, Message:{}", httpStatus, e.getMessage(), e);

        return ErrorResponse.builder()
                .message(message)
                .detailedMessage(e.getMessage())
                .dateTime(OffsetDateTime.now()).build();
    }
}
