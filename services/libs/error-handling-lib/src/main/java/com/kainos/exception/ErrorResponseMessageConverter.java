package com.kainos.exception;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import com.kainos.pets.api.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Explicit Message converter for the ErrorResponse to cover all media types. So we always return it as a JSON
 *
 * @see com.kainos.pets.api.model.ErrorResponse
 */

@Component
public class ErrorResponseMessageConverter extends AbstractHttpMessageConverter<ErrorResponse> {

    @Autowired
    private ObjectMapper objectMapper;

    public ErrorResponseMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return ErrorResponse.class.getSimpleName().equals(aClass.getSimpleName());
    }

    @Override
    protected ErrorResponse readInternal(Class<? extends ErrorResponse> aClass, HttpInputMessage httpInputMessage) throws IOException,
        HttpMessageNotReadableException {
        return objectMapper.readValue(httpInputMessage.getBody(), ErrorResponse.class);
    }

    @Override
    protected void writeInternal(ErrorResponse errorResponse, HttpOutputMessage httpOutputMessage) throws IOException,
        HttpMessageNotWritableException {
        objectMapper.writeValue(httpOutputMessage.getBody(), errorResponse);
    }
}
