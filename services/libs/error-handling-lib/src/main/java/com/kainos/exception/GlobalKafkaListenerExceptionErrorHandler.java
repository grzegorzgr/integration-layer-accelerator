package com.kainos.exception;

import static java.util.Optional.ofNullable;

import static org.springframework.util.ObjectUtils.isEmpty;

import static com.kainos.mapper.SafeMapper.nullSafe;
import static com.kainos.tracing.TracingConfiguration.TRACE_ID_BAGGAGE;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;

import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.SeekUtils;
import org.springframework.stereotype.Component;

import com.kainos.consumermanagement.stopping.ConsumerStoppedService;
import com.kainos.errorreporting.ErrorReporter;
import com.kainos.tracing.TraceContextProvider;

import brave.kafka.clients.TracingConsumerIdExtractor;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(value = "etcd.endpoint")
public class GlobalKafkaListenerExceptionErrorHandler extends DefaultErrorHandler {

    //using this log so the SeeksUtil is satisfied
    // checked manually that this logger also works
    private static final LogAccessor APACHE_LOGGER =
        new LogAccessor(LogFactory.getLog(GlobalKafkaListenerExceptionErrorHandler.class)); // NOSONAR
    private static final BiPredicate<ConsumerRecord<?, ?>, Exception> NEVER_SKIP = (record, exception) -> false;

    @Autowired
    private TraceContextProvider traceContextProvider;

    @Autowired
    private ErrorReporter errorReporter;

    @Autowired
    private ConsumerStoppedService consumerStoppedService;

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer,
        MessageListenerContainer container) {
        if (!isEmpty(record)) {
            traceContextProvider.newSpanWithTraceId(getValueFromKafkaMsgHeader(record, TRACE_ID_BAGGAGE),
                "async-error-report", () -> {
                    CustomException exception = findException(thrownException);
                    errorReporter.reportMessage(exception);

                    if (exception instanceof TechnicalException) {
                        log.info("Pausing consumers on ILTechnicalException");
                        sendConsumersPauseSignalAndRewindMsg(consumerStoppedService, thrownException, record, consumer, exception);
                        moveOffsetBack(thrownException, record, consumer, container);
                    }
                });
        }
        return true;
    }

    private void moveOffsetBack(Exception exception, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer,
        MessageListenerContainer container) {
        super.handleRemaining(exception, List.of(record), consumer, container);
    }

    private void sendConsumersPauseSignalAndRewindMsg(
        ConsumerStoppedService consumerStoppedService, Exception thrownException,
        ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, CustomException ilException
    ) {
        // consumerId is the consumer-group-id with a suffix ('-n') which is added by Spring
        // to ensure uniqueness when concurrency is used e.g. "onboardings-propagation-0"
        var consumerId = TracingConsumerIdExtractor.extractId(consumer);
        var consumerName = getCommonPrefixForAllConsumerInstancesOfSpecificService(consumerId);

        try {
            String sourceSystem = "TODO";
            consumerStoppedService.pauseConsumerWithId(consumerName, sourceSystem);
        } catch (ExecutionException e) {
            log.error("Pausing exception", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Pausing exception", e);
        }

        log.info("Consumer rewind so the record will be processed again");
        SeekUtils.doSeeks(List.of(record), consumer, thrownException, true, NEVER_SKIP, APACHE_LOGGER);
    }

    private static String getCommonPrefixForAllConsumerInstancesOfSpecificService(String id) {
        // Spring consumers are named like: "onboardings-propagation-0", "onboardings-propagation-1"
        // we remove the last part (number) to have a common part e.g. "onboardings-propagation"
        var lastIndex = id.lastIndexOf('-');
        return id.substring(0, lastIndex);
    }

    private static String getValueFromKafkaMsgHeader(ConsumerRecord<?, ?> record, String headerName) {
        return ofNullable(record.headers().lastHeader(headerName))
            .or(() -> ofNullable(record.headers().lastHeader(headerName.toLowerCase())))
            .map(header -> nullSafe(() -> new String(header.value(), StandardCharsets.UTF_8)))
            .orElse("");
    }

    private CustomException findException(Throwable e) {
        Throwable nestedCause = e.getCause();

        if (e instanceof ListenerExecutionFailedException ex) {
            nestedCause = nullSafe(() -> ex.getCause().getCause());
        }

        if (nestedCause instanceof CustomException customException) {
            return customException;
        }

        if (nestedCause instanceof RetryableException ex) {
            return TechnicalException.builder()
                .cause(ex.getCause())
                .message(ex.getCause().getMessage())
                .build();
        } else {
            return UnknownException.builder()
                .cause(e)
                .message(e.getMessage())
                .build();
        }
    }
}
