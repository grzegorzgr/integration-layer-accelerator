package com.kainos.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogLevelMarkerType {
    FATAL("FATAL");

    private final String name;
}
