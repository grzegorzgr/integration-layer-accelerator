package com.kainos.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@ConfigurationProperties("http")
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("MagicNumber")
public class TimeoutProperties {
    private int readTimeoutMs = 60 * 1000;
    private int connectTimeoutMs = 60 * 1000;
    private int connectionRequestTimeoutMs = 60 * 1000;
    private int internalReadTimeoutMs = 120 * 1000;
    private int internalConnectTimeoutMs = 120 * 1000;
    private int internalConnectionRequestTimeoutMs = 120 * 1000;
}
