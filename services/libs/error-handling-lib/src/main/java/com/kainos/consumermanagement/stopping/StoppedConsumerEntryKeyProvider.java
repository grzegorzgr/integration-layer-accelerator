package com.kainos.consumermanagement.stopping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StoppedConsumerEntryKeyProvider {
    public static final String CONSUMER_CONFIG_KEY = "consumer";

    private final String applicationName;

    @Autowired
    public StoppedConsumerEntryKeyProvider(
        @Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    public String consumerConfigKey() {
        return CONSUMER_CONFIG_KEY + "." + applicationName;
    }

    public String consumerConfigKey(String consumerKey) {
        return CONSUMER_CONFIG_KEY + "." + applicationName + "." + consumerKey;
    }

    public String withoutApplicationKey(String id) {
        if (id.startsWith(applicationName)) {
            return id.replaceFirst(applicationName + "\\.", "");
        }
        return id;
    }
}
