package com.kainos.consumermanagement.configwatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadsConfiguration {

    @Bean("singleThreadExecutor")
    public ExecutorService singleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
