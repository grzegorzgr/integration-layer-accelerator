package com.kainos.gateway.api;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PreGatewayHeaderOrchestratorFilter extends AbstractGatewayFilterFactory<PreGatewayHeaderOrchestratorFilter.Config> {
    public static final String TRACE_ID_HEADER_NAME = "TRACE-ID";

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String requestTraceId = extractTraceIdValueFromRequest(exchange)
                .orElse(UUID.randomUUID().toString());

            log.info("Received request: {} '{}'. TRACE-ID: {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                requestTraceId);

            ServerHttpRequest requestWithTraceId = exchange.getRequest().mutate()
                .header(TRACE_ID_HEADER_NAME, requestTraceId)
                .build();

            ServerWebExchange newExchange = exchange.mutate().request(requestWithTraceId).build();

            assureTraceIdInResponse(newExchange, requestTraceId);

            return chain
                .filter(newExchange)
                .then(Mono.fromRunnable(() -> {
                    logResponse(newExchange);
                }));
        });
    }

    public PreGatewayHeaderOrchestratorFilter() {
        super(Config.class);
    }

    //if the underlying service doesn't provide the traceId, we put the traceId from the original request
    private void assureTraceIdInResponse(ServerWebExchange exchange, String requestTraceId) {
        String responseTraceId = exchange.getResponse().getHeaders().getFirst(TRACE_ID_HEADER_NAME);
        if (responseTraceId == null) {
            exchange.getResponse().getHeaders().add(TRACE_ID_HEADER_NAME, requestTraceId);
        }
    }

    private Optional<String> extractTraceIdValueFromRequest(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER_NAME));
    }

    private void logResponse(ServerWebExchange exchange) {
        log.info("Request {} returned code {}. TRACE-ID: {}",
            exchange.getRequest().getPath().value(),
            exchange.getResponse().getStatusCode(),
            exchange.getResponse().getHeaders().getFirst(TRACE_ID_HEADER_NAME));
    }

    public static class Config {
    }
}
