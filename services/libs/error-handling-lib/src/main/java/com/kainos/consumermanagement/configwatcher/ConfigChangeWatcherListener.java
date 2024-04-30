package com.kainos.consumermanagement.configwatcher;

import static java.nio.charset.Charset.defaultCharset;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(value = "etcd.endpoint")
public class ConfigChangeWatcherListener {

    private KafkaListenerEndpointRegistry registry;

    private final String applicationName;

    public ConfigChangeWatcherListener(
        KafkaListenerEndpointRegistry registry,
        @Value("${spring.application.name}") String applicationName) {
        this.registry = registry;
        this.applicationName = applicationName;
    }

    public void consume(WatchResponse response) {
        log.info("Watching for key={}", applicationName);

        for (WatchEvent event : response.getEvents()) {
            log.info("type={}, key={}, value={}",
                event.getEventType(),
                Optional.ofNullable(event.getKeyValue().getKey())
                    .map(k -> k.toString(defaultCharset()))
                    .orElse(""),
                Optional.ofNullable(event.getKeyValue().getValue())
                    .map(k -> k.toString(defaultCharset()))
                    .orElse("")
            );

            String keyString = event.getKeyValue().getKey().toString(defaultCharset());
            String listenerId = getListenerIdFromKeyValue(keyString);
            if (event.getEventType() == WatchEvent.EventType.DELETE) {
                log.info("Received DELETE event for: {}", keyString);
                var container = registry.getListenerContainer(listenerId);
                if (container != null) {
                    log.info("Resuming consumer {}", keyString);
                    container.resume();
                } else {
                    log.warn("Listener container {} not found during RESUME", keyString);
                }
            } else if (event.getEventType() == WatchEvent.EventType.PUT) {
                log.info("Received PUT event for: {}", keyString);
                var container = registry.getListenerContainer(listenerId);
                if (container != null) {
                    log.info("Pausing consumer {}", keyString);
                    container.pause();
                } else {
                    log.warn("Listener container {} not found during PAUSE", keyString);
                }
            } else {
                throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
            }
        }
    }

    private String getListenerIdFromKeyValue(String keyValue) {
        var split = keyValue.split("\\.");
        return split[split.length - 1];
    }
}
