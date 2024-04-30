package com.kainos.errorreporting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kainos.common.avro.ErrorEvent;
import com.kainos.exception.CustomException;
import com.kainos.kafka.KafkaProducer;
import com.kainos.properties.KafkaTopicsProperties;
import com.kainos.tracing.TraceContextProvider;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ErrorReporter {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private KafkaTopicsProperties topics;

    @Autowired
    private TraceContextProvider traceContextProvider;

    public void reportMessage(CustomException exception) {
        ErrorEvent errorEvent = ErrorEvent.newBuilder()
            .setTraceId(traceContextProvider.traceId())
            .setMessage(exception.getMessage())
            .build();
        kafkaProducer.send(topics.getErrors(), null, errorEvent);
    }
}
