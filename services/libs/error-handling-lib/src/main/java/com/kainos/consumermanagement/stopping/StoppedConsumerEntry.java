package com.kainos.consumermanagement.stopping;

import static java.nio.charset.Charset.defaultCharset;

import io.etcd.jetcd.KeyValue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoppedConsumerEntry {
    private String consumerIdentificator;
    private String stoppingReason;

    public StoppedConsumerEntry(KeyValue keyValue) {
        consumerIdentificator = keyValue.getKey().toString(defaultCharset());
        stoppingReason = keyValue.getValue().toString(defaultCharset());
    }
}

