package com.kainos.exception;

import lombok.Builder;

public class BusinessException extends CustomException {

    @Builder
    public BusinessException(Throwable cause, String message, String errorCode, String businessIds) {
        super(cause, message, errorCode, businessIds);
    }
}
