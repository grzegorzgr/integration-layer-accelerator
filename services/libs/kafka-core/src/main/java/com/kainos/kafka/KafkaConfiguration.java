package com.kainos.kafka;

import static org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD;

import java.io.IOException;
import java.util.Map;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import com.kainos.kafka.redactor.RedactorPropertiesProvider;
import com.kainos.kafka.ssl.SslPropertiesProvider;
import com.kainos.logging.KafkaConsumerLoggingInterceptor;
import com.kainos.tracing.TraceContextProvider;

@Configuration
@ConditionalOnProperty(value = "etcd.endpoint")
public class KafkaConfiguration {
    private static final BackOff NO_RETRIES_OR_DELAY_BACKOFF = new FixedBackOff(0L, 0L);

    @Bean
    public <K, V> ProducerFactory<K, V> kafkaProducerFactory(KafkaProperties properties, SslPropertiesProvider sslPropertiesProvider,
        RedactorPropertiesProvider redactorPropertiesProvider) throws IOException {

        Map<String, Object> producerProperties = properties.buildProducerProperties();
        redactorPropertiesProvider.setJsonRedactor(producerProperties);
        sslPropertiesProvider.setSslCertsLocation(producerProperties);

        DefaultKafkaProducerFactory<K, V> factory = new DefaultKafkaProducerFactory<>(producerProperties);
        String transactionIdPrefix = properties.getProducer().getTransactionIdPrefix();

        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        return factory;
    }

    @Bean
    public <K, V> ConsumerFactory<K, V> kafkaConsumerFactory(KafkaProperties properties, SslPropertiesProvider sslPropertiesProvider,
        RedactorPropertiesProvider redactorPropertiesProvider, TraceContextProvider traceContextProvider) throws IOException {

        Map<String, Object> consumerProperties = properties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaConsumerLoggingInterceptor.class.getName());
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        redactorPropertiesProvider.setJsonRedactor(consumerProperties);
        sslPropertiesProvider.setSslCertsLocation(consumerProperties);

        KafkaConsumerLoggingInterceptor.inject(traceContextProvider);

        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, SpecificRecordBase> noRetryableKafkaListenerContainerFactory(
        DefaultErrorHandler defaultErrorHandler,
        @Qualifier("kafkaConsumerFactory") ConsumerFactory<String, SpecificRecordBase> consumerFactory) {

        defaultErrorHandler.setSeekAfterError(false);
        defaultErrorHandler.setBackOffFunction(((consumerRecord, e) -> NO_RETRIES_OR_DELAY_BACKOFF));

        ConcurrentKafkaListenerContainerFactory<String, SpecificRecordBase> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler);
        factory.getContainerProperties().setAckMode(RECORD);

        return factory;
    }
}
