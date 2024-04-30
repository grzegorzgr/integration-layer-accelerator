package brave.kafka.clients;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Why this is needed?
 * Look at the package, now back here, and again at the package.
 * We needed to access the TracingConsumer from brave.
 * As the way Sleuth work is wrapping KafkaConsumer inside the TracingConsumer
 * We recurse once to get down to it. Another surprise! The client id is private.
 * We use reflection only to read the client id which is needed to for notifying
 * the coordinated shutdown system.
 * Alternative would be creating our own version of KafkaConsumer,
 * but would need to wire it on everywhere.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TracingConsumerIdExtractor {
    private static final String CLIENT_ID_FIELD_NAME = "clientId";

    public static String extractId(Consumer<?, ?> consumer) {
        if (consumer == null) {
            return null;
        }

        if (consumer instanceof TracingConsumer) {
            var tracingConsumer = (TracingConsumer) consumer;
            return extractId(tracingConsumer.delegate);
        }
        if (consumer instanceof KafkaConsumer) {
            try {
                var kafkaConsumer = (KafkaConsumer) consumer;

                return FieldUtils.readField(kafkaConsumer, CLIENT_ID_FIELD_NAME, true).toString();
            } catch (IllegalAccessException e) {
                log.warn("Unable to retrieve {} by reflection", CLIENT_ID_FIELD_NAME);
                return null;
            }
        }

        log.warn(
            "Unable to retrieve {}, consumers didn't match hierarchy, last consumer type {}",
            CLIENT_ID_FIELD_NAME,
            consumer.getClass().getCanonicalName());

        return null;
    }
}
