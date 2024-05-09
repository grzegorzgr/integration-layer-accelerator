package tests.clients;

import static tests.utils.NullSafeTransformationUtil.nullSafe;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tests.utils.TestSettings;

@Slf4j
public class KafkaConsumerClient {
    @Data
    public static class ConsumersResponse {
        private String consumerName;
        private List<String> sourceSystems;
    }

    private static final String TRACE_ID = "trace-id";

    public <T extends SpecificRecordBase> List<T> getAllMsgsByTraceId(String topic, Schema avroSchema, String expectedTraceId) {
        Predicate<ConsumerRecord<String, T>> traceIdPredicate = record -> {
            byte[] msgTraceId = nullSafe(() -> record.headers().lastHeader(TRACE_ID).value());

            String parsedTraceId = msgTraceId != null ? new String(msgTraceId, StandardCharsets.UTF_8) : null;

            log.info("Found Kafka msg with trace-id: {}, expecting {}", parsedTraceId, expectedTraceId);

            if (parsedTraceId != null) {
                return parsedTraceId.equals(expectedTraceId);
            }

            // alternate predicate that checks msg body (legacy solution)
            return record.value() != null
                && record.value().toString().contains("traceId")
                && record.value().get("traceId").toString().equals(expectedTraceId);
        };

        return getAllMsgsWithRecordPredicate(topic, avroSchema, traceIdPredicate);
    }

    private <T extends SpecificRecordBase> List<T> getAllMsgsWithRecordPredicate(String topic, Schema avroSchema,
        Predicate<ConsumerRecord<String, T>> consumerRecordPredicate) {

        List<ConsumerRecord> allMsgs = KafkaConsumerInternal.pollAndGetAllAvroMsgsFromTopic(topic);

        return allMsgs.stream()
            .map(record -> (ConsumerRecord<String, T>) record)
            .filter(consumerRecordPredicate)
            .map(record -> SpecificData.get().deepCopy(avroSchema, record.value()))
            .collect(Collectors.toList());
    }
}
