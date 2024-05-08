package com.kainos.exception;

import lombok.Builder;

public class BusinessException extends CustomException {

    @Builder
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
