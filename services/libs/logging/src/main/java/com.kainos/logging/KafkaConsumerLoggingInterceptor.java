package com.kainos.logging;

import static java.nio.charset.StandardCharsets.UTF_8;

import static com.kainos.mapper.SafeMapper.nullSafe;

import java.util.Map;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;

import com.kainos.logging.sensitivedata.redactor.JsonRedactor;
import com.kainos.tracing.TraceContextProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaConsumerLoggingInterceptor implements ConsumerInterceptor<String, SpecificRecordBase> {
    public static final String KAFKA_HEADER_TRACE_ID = "TRACE-ID";

    // will be provided by this.configure( )
    private JsonRedactor jsonRedactor;
    private static TraceContextProvider traceContextProvider;

    @Override
    public ConsumerRecords<String, SpecificRecordBase> onConsume(ConsumerRecords<String, SpecificRecordBase> records) {
        records.forEach(record -> {
            var cr = ((ConsumerRecord) record);

            String traceId = getKafkaHeaderValue(record, KAFKA_HEADER_TRACE_ID);

            // This Kafka interceptor runs in separate thread.
            // It does not have any context set.
            // We manually setup context based on Kafka msg headers
            // to have this log annotated with correct metadata.
            traceContextProvider.newSpanWithTraceId(traceId, "kafka-on-consume", () -> {
                logKafkaPayload(cr);
            });
        });
        return records;
    }

    public static void inject(TraceContextProvider traceContextProvider) {
        KafkaConsumerLoggingInterceptor.traceContextProvider = traceContextProvider;
    }

    private String getKafkaHeaderValue(ConsumerRecord<String, SpecificRecordBase> record, String headerName) {
        String headerValue = "";

        Header header = record.headers()
            //headers in kafka are lower cased
            .lastHeader(headerName.toLowerCase());

        if (header != null) {
            headerValue = nullSafe(() -> new String(header.value(), UTF_8));
        }

        return headerValue;
    }

    private void logKafkaPayload(ConsumerRecord cr) {
        log.info("Reading from kafka: topic: {}, partition: {}, offset: {}, key: {}",
            cr.topic(), cr.partition(), cr.offset(), cr.key());
        if (cr.value() != null) {
            var payload = cr.value().toString();
            log.info(jsonRedactor.redact(payload));
        }
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
        // left empty by design
    }

    @Override
    public void close() {
        // left empty by design
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // this interceptor is outside Spring context
        this.jsonRedactor = (JsonRedactor) configs.get("jsonRedactor.bean");
    }
}
