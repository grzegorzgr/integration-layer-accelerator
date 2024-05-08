package com.kainos.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.modelmapper.MappingException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.kainos.pets.api.model.Error;
import com.kainos.pets.api.model.ErrorResponse;
import com.kainos.tracing.TraceContextProvider;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler extends ExceptionHandlerExceptionResolver {
    private static final String GATEWAY_TIMEOUT_MESSAGE = "Connection timeout error";
    private static final String INCORRECT_REQUEST_DATA_MESSAGE = "Incorrect request data";
    private static final String REQUEST_FAILED_MESSAGE = "REQUEST_FAILED";
    private static final String MEDIA_TYPE_NOT_ACCEPTABLE_MESSAGE = "Media type not acceptable";
    private static final String MEDIA_TYPE_NOT_SUPPORTED_MESSAGE = "Media type not supported";
    private static final String REQUEST_METHOD_NOT_SUPPORTED_MESSAGE = "Method is not allowed";

    @Autowired
    private TraceContextProvider traceContextProvider;

    /**
     * CustomExceptions handlers
     */

    @ResponseStatus(GATEWAY_TIMEOUT)
    @ExceptionHandler(TechnicalException.class)
    @ResponseBody
    public ErrorResponse handleTechnicalException(TechnicalException exception) {
        logException(exception);
        return createErrorResponse(GATEWAY_TIMEOUT_MESSAGE, exception, GATEWAY_TIMEOUT.value());
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UnknownException.class)
    @ResponseBody
    public ErrorResponse handleUnknownException(UnknownException exception) {
        logException(exception);
        return createErrorResponse(REQUEST_FAILED_MESSAGE, exception, INTERNAL_SERVER_ERROR.value());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ErrorResponse handleBusinessException(BusinessException exception) {
        logException(exception);
        return createErrorResponse(INCORRECT_REQUEST_DATA_MESSAGE, exception, BAD_REQUEST.value());
    }

    /**
     * Standard spring exceptions handlers
     */

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
        ConversionNotSupportedException.class,
        ExecutionException.class,
        HttpMessageNotWritableException.class,
        MappingException.class
    })
    @ResponseBody
    public ErrorResponse handleInternalServerError(Exception exception) {
        logException(exception);
        return createErrorResponse(REQUEST_FAILED_MESSAGE, exception, INTERNAL_SERVER_ERROR.value());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ErrorResponse handleBindException(BindException exception) {
        logException(exception);
        var fieldErrors = exception.getFieldErrors()
            .stream()
            .map(error -> new Error().message(error.getField() + " " + error.getDefaultMessage()).path(error.getField()))
            .collect(Collectors.toList());
        return createErrorResponseWithMultipleErrors(
            INCORRECT_REQUEST_DATA_MESSAGE, fieldErrors, BAD_REQUEST.value());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        logException(exception);
        var fieldErrors = exception.getBindingResult().getFieldErrors()
            .stream()
            .map(error -> new Error().message(error.getDefaultMessage()).path(error.getField()))
            .collect(Collectors.toList());
        return createErrorResponseWithMultipleErrors(
            INCORRECT_REQUEST_DATA_MESSAGE, fieldErrors, BAD_REQUEST.value());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({
        DateTimeParseException.class,
        HttpMessageConversionException.class,
        HttpMessageNotReadableException.class,
        IllegalArgumentException.class,
        MissingServletRequestParameterException.class,
        MissingServletRequestPartException.class,
        TypeMismatchException.class,
        MultipartException.class
    })
    @ResponseBody
    public ErrorResponse handleBadRequest(Exception exception) {
        logException(exception);
        return createErrorResponse(INCORRECT_REQUEST_DATA_MESSAGE, exception, BAD_REQUEST.value());
    }

    @ResponseStatus(NOT_ACCEPTABLE)
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    public ErrorResponse handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException exception) {
        String message = extractFullMessageFromException(exception);
        message += ". Supported media types: " + exception.getSupportedMediaTypes();

        logException(exception);

        return createErrorResponse(MEDIA_TYPE_NOT_ACCEPTABLE_MESSAGE, message, NOT_ACCEPTABLE.value());
    }

    @ResponseStatus(UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public ErrorResponse handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
        String message = extractFullMessageFromException(exception);

        message += ". Supported media types: " + exception.getSupportedMediaTypes();

        logException(exception);

        return createErrorResponse(MEDIA_TYPE_NOT_SUPPORTED_MESSAGE, message, UNSUPPORTED_MEDIA_TYPE.value());
    }

    @ResponseStatus(METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ErrorResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        String message = extractFullMessageFromException(exception);
        logException(exception);
        return createErrorResponse(REQUEST_METHOD_NOT_SUPPORTED_MESSAGE, message, METHOD_NOT_ALLOWED.value());
    }

    /**
     * All unknown exceptions handler
     */

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse handleUnknownException(Exception exception) {
        logException(exception);
        return createErrorResponse(REQUEST_FAILED_MESSAGE, exception, INTERNAL_SERVER_ERROR.value());
    }

    private ErrorResponse createErrorResponse(String key, Exception exception, Integer errorCode) {
        return new ErrorResponse()
            .key(key)
            .code(errorCode)
            .errors(List.of(new Error().message(extractFullMessageFromException(exception))))
            .traceId(traceContextProvider.traceId());
    }

    private ErrorResponse createErrorResponse(String key, String message, Integer errorCode) {
        return new ErrorResponse()
            .key(key)
            .code(errorCode)
            .errors(List.of(new Error().message(message)))
            .traceId(traceContextProvider.traceId());
    }

    private ErrorResponse createErrorResponseWithMultipleErrors(String key, List<Error> errors, Integer errorCode) {
        return new ErrorResponse()
            .key(key)
            .code(errorCode)
            .errors(errors)
            .traceId(traceContextProvider.traceId());
    }

    private void logException(Exception exception) {
        CustomException customException;
        if (exception instanceof CustomException) {
            customException = (CustomException) exception;
        } else {
            customException = UnknownException.builder()
                .cause(exception)
                .message(extractFullMessageFromException(exception))
                .build();
        }

        log.error(customException.getMessage(), customException.getCause());
    }

    private String extractFullMessageFromException(Exception e) {
        String message = e.getMessage();
        Throwable cause = e.getCause();

        if (cause != null) {
            message = String.format("%s; %s", message, cause.getMessage());
        }
        return message;
    }
}
