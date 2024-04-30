package com.kainos.exception;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "NO MESSAGE IN EXCEPTION";

    //private SourceSystem sourceSystem = SourceSystem.IL;
    private String errorCode;
    private String businessId;
    private String errorId;

    // business object is added using AOP & annotations
    //private BusinessObjectType businessObjectType;

    CustomException(Throwable cause, String message, String errorCode, String businessId) {
        super(message != null ? message : DEFAULT_MESSAGE);

//        if (sourceSystem != null) {
//            this.sourceSystem = sourceSystem;
//        }

        if (cause != null) {
            this.initCause(cause);
        }

        this.businessId = businessId;
        this.errorCode = errorCode;
    }

//    public String getFullErrorCode() {
//        return FullErrorCodeGenerator.generateFullErrorCode(this);
//    }

    public String getErrorId() {
        if (this.errorId == null) {
            this.errorId = UUID.randomUUID().toString();
        }
        return this.errorId;
    }
}

