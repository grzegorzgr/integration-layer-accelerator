package com.kainos.errorhandling;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.kainos.common.avro.ErrorEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ErrorHandlingService {

    @KafkaListener(topics = "${kafka.topics.errors}", groupId = "${spring.kafka.consumer.group-id}", id = "errorHandling",
        containerFactory = "simpleKafkaContainerFactory")
    public void listen(ConsumerRecord<String, ErrorEvent> consumerRecord) {
        log.info("Receiving error to handle");
        // custom error handling logic
    }
}

