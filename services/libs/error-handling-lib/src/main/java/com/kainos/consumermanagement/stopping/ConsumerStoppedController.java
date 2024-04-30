package com.kainos.consumermanagement.stopping;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kainos.model.ConsumersResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "etcd.endpoint")
public class ConsumerStoppedController {
    private final ConsumerStoppedService consumerStoppedService;

    @GetMapping("/consumers")
    public List<ConsumersResponse> stoppedConsumers() {
        List<ConsumersResponse> consumersResponses = consumerStoppedService.getStoppedConsumersForCurrentApplication();
        log.info("Returning consumers responses: {}", consumersResponses);
        return consumersResponses;
    }

    @PostMapping("/consumers/{consumer}")
    public void stopConsumer(@PathVariable String consumer) throws ExecutionException, InterruptedException {
        consumerStoppedService.pauseConsumerWithId(consumer, "manual");
    }

    @DeleteMapping("/consumers/{consumer}")
    public void resumeConsumer(@PathVariable String consumer) throws ExecutionException, InterruptedException {
        consumerStoppedService.resumeConsumerWithId(consumer);
    }
}

