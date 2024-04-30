package com.kainos.properties;

import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@ConfigurationProperties("kafka.topics")
@Getter
@Setter
@NoArgsConstructor
@Validated
public class KafkaTopicsProperties {

    @NotNull
    private String errors = "errors";

    @NotNull
    private String operationStatuses = "operationStatuses";
}

