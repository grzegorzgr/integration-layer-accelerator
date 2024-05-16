package com.kainos.restclient;

import java.net.URL;
import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RestClientConfig {
    private URL keyStore;
    private String keyPassword;
    private String keyStorePassword;
    private URL trustStore;
    private String trustStorePassword;
    private String serviceUrl;
    private boolean ignoreBadServerCertificate;
    private List<HttpMessageConverter<?>> customMessageConverters;
    private boolean useInternalTimeoutProperties;
}
