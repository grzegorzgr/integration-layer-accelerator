package com.kainos.kafka;

import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.kainos.exception.BusinessException;
import com.kainos.exception.TechnicalException;
import com.kainos.kafka.headers.KafkaRecordHeadersProvider;
import com.kainos.logging.sensitivedata.redactor.JsonRedactor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    @Autowired
    private KafkaRecordHeadersProvider kafkaRecordHeadersProvider;

    @Autowired
    private JsonRedactor jsonRedactor;

    public void send(String topic, String key, SpecificRecordBase data) {
        try {
            log.info("Attempt to write to Kafka topic {} with key:{}", topic, key);
            if (data != null) {
                log.info(jsonRedactor.redact(String.valueOf(data)));
            }
            List<Header> headers = kafkaRecordHeadersProvider.createHeaders();
            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, null, key, data, headers);
            SendResult<String, SpecificRecordBase> result = kafkaTemplate.send(record).get();

            RecordMetadata recordMetadata = result.getRecordMetadata();
            Integer partition = recordMetadata != null ? recordMetadata.partition() : null;
            Long offset = recordMetadata != null ? recordMetadata.offset() : null;
            log.info("Successful to write to Kafka: topic {},  partition: {}, offset: {},  key:{}",
                topic, partition, offset, key);
        } catch (SerializationException e) {
            throw BusinessException.builder()
                .message(String.format("Kafka serialization exception occurred on sending message to a topic %s with key %s", topic, key))
                .cause(e)
                .build();
        } catch (Exception e) {
            throw TechnicalException.builder()
                .message(String.format("Kafka exception occurred on sending message to a topic %s with key %s", topic, key))
                .cause(e)
                .build();
        }
    }
}

