package com.kainos.exception.decoder;

import static java.util.List.of;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.kainos.exception.BusinessException;
import com.kainos.exception.TechnicalException;
import com.kainos.exception.UnknownException;

import feign.Response;
import feign.codec.ErrorDecoder;

@Component
public class FeignClientErrorDecoder implements ErrorDecoder {
    private static final List<HttpStatus> BUSINESS_ERROR_STATUS_CODES = of(
        HttpStatus.BAD_REQUEST,
        HttpStatus.NOT_FOUND);

    @Override
    public Exception decode(String s, Response response) {
        HttpStatus responseStatus = HttpStatus.resolve(response.status());

        if (BUSINESS_ERROR_STATUS_CODES.contains(responseStatus)) {
            throw BusinessException.builder()
                .message(response.reason())
                .build();
        }

        if (responseStatus != null && responseStatus.is5xxServerError()) {
            throw TechnicalException.builder()
                .message(response.reason())
                .build();
        } else {
            throw UnknownException.builder()
                .message(response.reason())
                .build();
        }
    }
}
