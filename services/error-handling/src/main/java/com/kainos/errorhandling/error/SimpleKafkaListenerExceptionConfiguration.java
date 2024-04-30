package com.kainos.errorhandling.error;

import static org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SimpleKafkaListenerExceptionConfiguration {

    @Autowired
    private SkippingRecordCommonErrorHandler skippingRecordCommonErrorHandler;

    /**
     * This bean is necessary for error-handling to avoid the loops if you get an error when reporting
     * error to sfdc.
     * We won't report to SFDC errors caused by error handler.
     * Otherwise, we would have an infinite loops and create infinite messages.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SpecificRecordBase> simpleKafkaContainerFactory(
        @Qualifier("kafkaConsumerFactory") ConsumerFactory<String, SpecificRecordBase> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, SpecificRecordBase> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(skippingRecordCommonErrorHandler);
        factory.getContainerProperties().setAckMode(RECORD);

        return factory;
    }
}

