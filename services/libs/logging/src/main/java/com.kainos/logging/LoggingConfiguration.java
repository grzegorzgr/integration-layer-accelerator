package com.kainos.logging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kainos.logging.sensitivedata.redactor.JsonRedactor;
import com.kainos.logging.sensitivedata.redactor.properties.GlobalRedactorProperties;
import com.kainos.logging.sensitivedata.redactor.properties.JsonRedactorProperties;

@Configuration
@EnableConfigurationProperties(GlobalRedactorProperties.class)
public class LoggingConfiguration {

    @Bean
    public JsonRedactorProperties jsonRedactorProperties() {
        return new JsonRedactorProperties();
    }

    @Bean
    public GlobalRedactorProperties globalRedactorProperties() {
        return new GlobalRedactorProperties();
    }

    @Bean
    public JsonRedactor jsonRedactor() {
        return new JsonRedactor();
    }
}

