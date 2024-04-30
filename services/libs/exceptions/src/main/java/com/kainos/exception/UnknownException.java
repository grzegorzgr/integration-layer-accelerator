package com.kainos.exception;

import lombok.Builder;

public class UnknownException extends CustomException {

    @Builder
    public UnknownException(Throwable cause, String message, String errorCode, String businessIds) {
        super(cause, message, errorCode, businessIds);
    }
}
