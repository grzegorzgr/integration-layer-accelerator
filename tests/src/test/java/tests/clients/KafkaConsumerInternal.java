package tests.clients;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import static com.google.common.collect.ImmutableList.of;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.jetbrains.annotations.NotNull;

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import tests.utils.TestSettings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:magicnumber")
class KafkaConsumerInternal {

    private static final TestSettings TEST_SETTINGS = TestSettings.getInstance();

    private static final Map<String, KafkaConsumer> CONSUMERS_BY_TOPIC = new ConcurrentHashMap<>();
    private static final Map<String, List<ConsumerRecord>> MSGS_BY_TOPIC = new ConcurrentHashMap<>();

    private static final String RUNNER_ID = UUID.randomUUID().toString();

    private static final int KAFKA_POLL_TIME_MS = 1000;

    static {
        log.info("Runner ID for Kafka Consumer is {}", RUNNER_ID);
    }

    static <T extends SpecificRecordBase> List<ConsumerRecord> pollAndGetAllAvroMsgsFromTopic(String topic) {
        sleep100Ms();

        synchronized (KafkaConsumerInternal.class) {
            KafkaConsumer<String, T> consumer = getExistingOrCreateKafkaAvroConsumerForTopic(topic);
            ConsumerRecords<String, T> pollResult = singleAvroPoll(topic, consumer);
            List<ConsumerRecord> consumerRecords = processPollResult(pollResult, topic);
            consumer.commitSync();
            return consumerRecords;
        }
    }

    static <T> List<ConsumerRecord> pollAndGetAllMsgsFromTopic(String topic) {
        sleep100Ms();

        synchronized (KafkaConsumerInternal.class) {
            KafkaConsumer<String, T> consumer = getExistingOrCreateKafkaJsonConsumerForTopic(topic);
            ConsumerRecords<String, T> pollResult = singlePoll(topic, consumer);
            List<ConsumerRecord> consumerRecords = processPollResult(pollResult, topic);
            consumer.commitSync();
            return consumerRecords;
        }
    }

    private static void sleep100Ms() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static <T> List<ConsumerRecord> processPollResult(ConsumerRecords<String, T> pollResult, String topic) {
        List<ConsumerRecord<String, T>> newRecordsList = iteratorToList(pollResult.iterator());

        newRecordsList.forEach(record -> {
            Header traceIdHeader = record.headers().lastHeader("trace-id");
            String traceId = traceIdHeader != null ? new String(traceIdHeader.value(), StandardCharsets.UTF_8) : null;
            log.info("New Kafka msg in per-topic cache (topic {}, traceId {}) key {},  value {}",
                topic, traceId, record.key(), record.value());
        });

        MSGS_BY_TOPIC.computeIfAbsent(topic, k -> new ArrayList<>());
        List<ConsumerRecord> allMsgsForThisTopic = MSGS_BY_TOPIC.get(topic);
        allMsgsForThisTopic.addAll(newRecordsList);
        return allMsgsForThisTopic;
    }

    @NotNull
    private static  <T extends SpecificRecordBase> ConsumerRecords<String, T> singleAvroPoll(
        String topic, KafkaConsumer<String, T> consumer) {
        long currentTimeMillis = System.currentTimeMillis();
        log.info("Poll start for topic {}", topic);
        ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(KAFKA_POLL_TIME_MS));
        long currentTimeMillis1 = System.currentTimeMillis();

        log.info("Poll for topic {} (duration: {} ms) returned messages (number {}): {}",
            topic, currentTimeMillis1 - currentTimeMillis, records.count(), records);
        return records;
    }

    @NotNull
    private static <T> ConsumerRecords<String, T> singlePoll(String topic, KafkaConsumer<String, T> consumer) {
        long currentTimeMillis = System.currentTimeMillis();
        log.info("Poll start for topic {}", topic);
        ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(KAFKA_POLL_TIME_MS));
        long currentTimeMillis1 = System.currentTimeMillis();

        log.info("Poll for topic {} (duration: {} ms) returned messages (number {}): {}",
            topic, currentTimeMillis1 - currentTimeMillis, records.count(), records);
        return records;
    }

    private static <T extends SpecificRecordBase> KafkaConsumer<String, T> getExistingOrCreateKafkaAvroConsumerForTopic(String topic) {
        return getExistingOrCreateKafkaConsumerForTopic(topic, getTopicSpecificKafkaAvroProperties(topic));
    }

    private static <T> KafkaConsumer<String, T> getExistingOrCreateKafkaJsonConsumerForTopic(String topic) {
        return getExistingOrCreateKafkaConsumerForTopic(topic, getTopicSpecificKafkaProperties(topic));
    }

    private static <T> KafkaConsumer<String, T> getExistingOrCreateKafkaConsumerForTopic(
        String topic, Properties consumerProperties) {
        KafkaConsumer<String, T> consumer;
        if (!CONSUMERS_BY_TOPIC.containsKey(topic)) {
            log.info("Consumer for topic {} does not exist; creating new one", topic);
            consumer = new KafkaConsumer(consumerProperties);
            consumer.subscribe(of(topic));

            CONSUMERS_BY_TOPIC.put(topic, consumer);
        } else {
            log.info("Consumer for topic {} exists", topic);
            consumer = CONSUMERS_BY_TOPIC.get(topic);
        }
        return consumer;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private static Properties getTopicSpecificKafkaAvroProperties(String topic) {
        Properties properties = getCommonTopicSpecificKafkaProperties(topic);
        properties.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, TEST_SETTINGS.getProperty("kafka.value-deserializer"));
        properties.setProperty("schema.registry.url", TEST_SETTINGS.getProperty("kafka.schema_registry"));
        properties.setProperty(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        return properties;
    }

    private static Properties getTopicSpecificKafkaProperties(String topic) {
        Properties properties = getCommonTopicSpecificKafkaProperties(topic);
//        properties.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, TEST_SETTINGS.getProperty("kafka.value-json-deserializer"));
        return properties;
    }

    private static Properties getCommonTopicSpecificKafkaProperties(String topic) {
        Properties properties = new Properties();
        properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, TEST_SETTINGS.getProperty("kafka.broker"));
        // This is important to have separate group for each consumer (and those are topic-specific) to avoid rebalancing process
        // With multithreaded serenity setup we need to have unique groups so each consumer is standalone and eats all messages
        properties.setProperty(GROUP_ID_CONFIG, "tests-" + RUNNER_ID + "-" + topic);
        properties.setProperty(AUTO_OFFSET_RESET_CONFIG, TEST_SETTINGS.getProperty("kafka.auto-offset-reset"));
        properties.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, TEST_SETTINGS.getProperty("kafka.key-deserializer"));
        // Our consumers here do not poll all the time, only on specific test steps
        properties.setProperty(MAX_POLL_INTERVAL_MS_CONFIG, String.valueOf(Duration.ofMinutes(5).toMillis()));
        properties.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.setProperty(SESSION_TIMEOUT_MS_CONFIG, String.valueOf(Duration.ofSeconds(10).toMillis()));
        properties.setProperty(HEARTBEAT_INTERVAL_MS_CONFIG, String.valueOf(Duration.ofSeconds(5).toMillis()));

        return properties;
    }

    private static  <T> List<T> iteratorToList(Iterator<T> iterator) {
        ArrayList<T> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }
}