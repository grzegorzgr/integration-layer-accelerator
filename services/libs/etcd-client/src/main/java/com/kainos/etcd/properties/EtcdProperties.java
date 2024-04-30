package com.kainos.etcd.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties("etcd")
@Data
public class EtcdProperties {
    private String endpoint;
}
