package com.kainos.restclient;

import org.apache.hc.client5.http.classic.HttpClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class TimeoutableHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    public TimeoutableHttpComponentsClientHttpRequestFactory(HttpClient httpClient, int connectionTimeout, int connectionRequestTimeout) {
        super(httpClient);
        setTimeouts(connectionTimeout, connectionRequestTimeout);
    }

    private void setTimeouts(int connectionTimeout, int connectionRequestTimeout) {
        this.setConnectTimeout(connectionTimeout);
        this.setConnectionRequestTimeout(connectionRequestTimeout);
    }
}
