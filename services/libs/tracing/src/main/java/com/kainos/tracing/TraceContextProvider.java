package com.kainos.tracing;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import brave.baggage.BaggageField;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TraceContextProvider {
    private static final String TRUE_BAGGAGE_VALUE = "t";
    private static final String EMPTY_BAGGAGE_VALUE = "";

    @Autowired
    private BaggageField traceIdHeader;

    @Autowired
    private BaggageField isAsyncBaggage;

    @Autowired
    private BaggageField errorIdBaggage;

    @Autowired
    private BaggageField errorCodeBaggage;

    @Autowired
    private BaggageField errorMessageBaggage;

    @Autowired
    private Tracer tracer;

    public String traceId() {
        return traceIdHeader.getValue();
    }

    public void setErrorContext(String message, String errorCode, String errorId) {
        if (message == null) {
            message = "";
        }
        if (errorCode == null) {
            errorCode = "";
        }
        if (errorId == null) {
            errorId = "";
        }

        errorIdBaggage.updateValue(errorId);
        errorCodeBaggage.updateValue(errorCode);
        errorMessageBaggage.updateValue(message);
    }

    public void newRootSpan(Runnable body) {
        Span newSpan = getTracer().nextSpan().name("root_span");
        try (Tracer.SpanInScope ws = getTracer().withSpan(newSpan.start())) {
            body.run();
        } finally {
            newSpan.end();
        }
    }

    @FunctionalInterface
    public interface SupplierWithException<T, E extends Throwable> {
        T get() throws E;
    }

    public void setTraceId(String newTraceId) {
        log.info("Setting traceId explicitly to {}", newTraceId);
        setValueInBaggage(traceIdHeader, newTraceId);
    }

    public void newSpanWithTraceId(String traceId, String spanName, Runnable body) {
        Span newSpan = getTracer().nextSpan().name(spanName);
        try (Tracer.SpanInScope ws = getTracer().withSpan(newSpan.start())) {
            setValueInBaggage(traceIdHeader, traceId);
            newRootSpan(() -> body.run());
        } finally {
            cleanupTraceId();
            newSpan.end();
        }
    }

    public boolean isMarkedAsExecutionInAsynchronousProcess() {
        return Objects.equals(isAsyncBaggage.getValue(), TRUE_BAGGAGE_VALUE);
    }

    public String generateTraceId() {
        return UUID.randomUUID().toString();
    }


    private void cleanupTraceId() {
        setValueInBaggage(traceIdHeader, EMPTY_BAGGAGE_VALUE);
    }

    private Span getSpanOrStartNewIfNoSpan() {
        Span span = getTracer().currentSpan();

        if (span == null) {
            span = getTracer().nextSpan();

            if (!span.isNoop()) {
                // incur timestamp overhead only once
                span.start();
            }
        }

        return span;
    }

    private void setValueInBaggage(BaggageField baggageField, String value) {
        if (Objects.equals(baggageField.getValue(), value)) {
            log.trace("BaggageField:Value - {}:{} already set in", baggageField.name(), value);
            return;
        }

        boolean isOperationSuccessful = baggageField.updateValue(value);
        if (!isOperationSuccessful) {
            log.warn("BaggageField:Value - {}:{} - updateValue failed to update with value:{}",
                baggageField.name(), baggageField.getValue(), value);
        }
    }

    private Tracer getTracer() {
        return this.tracer;
    }
}

