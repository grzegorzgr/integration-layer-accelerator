package com.kainos.tracing;

import static com.kainos.tracing.TracingConfiguration.TRACE_ID_BAGGAGE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class TraceIdFeignClientRequestInterceptor {

    @Autowired
    private TraceContextProvider traceContextProvider;

    @Bean
    public RequestInterceptor traceIdInterceptor() {
        return template -> {
            template.header(TRACE_ID_BAGGAGE, traceContextProvider.traceId());
        };
    }
}
