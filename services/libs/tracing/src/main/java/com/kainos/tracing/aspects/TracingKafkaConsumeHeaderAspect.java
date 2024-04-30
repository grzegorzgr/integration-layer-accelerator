package com.kainos.tracing.aspects;

import static java.nio.charset.StandardCharsets.UTF_8;

import static com.kainos.tracing.TracingConfiguration.TRACE_ID_BAGGAGE;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.kainos.tracing.TraceContextProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Order(1)
@Component
public class TracingKafkaConsumeHeaderAspect {

    @Autowired
    private TraceContextProvider traceContextProvider;

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public void addTraceIdFromKafkaHeader(ProceedingJoinPoint proceedingJoinPoint) {
        Object[] signatureArgs = proceedingJoinPoint.getArgs();
        String traceId = "kafkaTraceIdNotFound";

        for (Object signatureArg : signatureArgs) {
            if (signatureArg instanceof ConsumerRecord consumerRecord) {
                traceId = getKafkaHeaderValue(consumerRecord, TRACE_ID_BAGGAGE);
            }
        }

        traceContextProvider.newSpanWithTraceId(traceId, "kafka-consumer-aspect", () -> {
            try {
                proceedingJoinPoint.proceed(signatureArgs);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String getKafkaHeaderValue(ConsumerRecord record, String headerName) {
        String headerValue = "";
        Header header = record.headers()
            //headers in kafka are lower cased
            .lastHeader(headerName.toLowerCase());

        if (header != null && header.value() != null) {
            headerValue = new String(header.value(), UTF_8);
        }

        return headerValue;
    }
}
