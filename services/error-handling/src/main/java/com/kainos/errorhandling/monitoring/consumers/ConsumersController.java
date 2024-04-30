package com.kainos.errorhandling.monitoring.consumers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kainos.model.ConsumersResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ConsumersController {

    @Autowired
    private ConsumersService consumersService;

    @GetMapping("/operations-service/paused-consumers")
    public List<ConsumersResponse> stoppedConsumers() {
        log.info("Received request to return all stopped consumers.");
        return consumersService.getAllPausedConsumersResponse();
    }

    @PostMapping("/operations-service/paused-consumers/resume")
    public void resumeConsumers(@RequestParam(value = "source", required = false) String errorSource) {
        if (errorSource != null) {
            log.info("Received request to resume consumers with error source {}.", errorSource);
            consumersService.resumeConsumerByErrorSource(errorSource);
        } else {
            log.info("Received request to resume all stopped consumers.");
            consumersService.resumeAll();
        }
    }
}

