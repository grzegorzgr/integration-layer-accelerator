package com.kainos.exception;

import lombok.Builder;

public class TechnicalException extends CustomException {

    @Builder
    public TechnicalException(Throwable cause, String message, String errorCode, String businessIds) {
        super(cause, message, errorCode, businessIds);
    }
}
