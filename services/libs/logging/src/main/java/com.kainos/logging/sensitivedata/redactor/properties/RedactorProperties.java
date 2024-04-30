package com.kainos.logging.sensitivedata.redactor.properties;

import lombok.Data;

@Data
class RedactorProperties {
    private String redactedStamp = "---REDACTED---";
    private String redactedArrayStamp = "---[REDACTED ARRAY]---";
}

