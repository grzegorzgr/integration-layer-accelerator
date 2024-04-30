package com.kainos.errorhandling.error;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SkippingRecordCommonErrorHandler implements CommonErrorHandler {

    @Value("${kafka.topics.errors}")
    private String errorsTopicName;

    @Override
    public boolean handleOne(
        Exception thrownException, ConsumerRecord<?, ?> record,
        Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.error("An error occurred during handling message from kafka topic {}. Failed record processing skipped", errorsTopicName);
        return CommonErrorHandler.super.handleOne(thrownException, record, consumer, container);
    }
}

