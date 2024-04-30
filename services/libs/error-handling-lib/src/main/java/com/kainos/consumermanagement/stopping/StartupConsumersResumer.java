package com.kainos.consumermanagement.stopping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(value = "etcd.endpoint")
public class StartupConsumersResumer implements ApplicationListener<ContextRefreshedEvent> {

    private ConsumerStoppedService consumerStoppedService;

    @Autowired
    public StartupConsumersResumer(ConsumerStoppedService consumerStoppedService) {
        this.consumerStoppedService = consumerStoppedService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // this is needed for tell etcd that consumers are working, in case of restart service with stopped consumers
        log.info("Resume all kafka consumers for this service");
        try {
            consumerStoppedService.resumeAllConsumersForCurrentService();
        } catch (Exception e) {
            log.error("Unable to resume all consumers for this service at startup, {}", e);
        }
    }
}
