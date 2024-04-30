package com.kainos.etcd;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kainos.etcd.client.EtcdClientWrapper;
import com.kainos.etcd.jobstorage.EtcdJobStorage;
import com.kainos.etcd.jobstorage.helpers.EtcdGenericSerializer;
import com.kainos.etcd.properties.EtcdProperties;

import io.etcd.jetcd.Client;

@Configuration
@EnableConfigurationProperties(EtcdProperties.class)
@ConditionalOnProperty(value = "etcd.endpoint")
public class EtcdConfiguration {

    @Bean
    public EtcdClientWrapper etcdClientWrapper() {
        return new EtcdClientWrapper();
    }

    @Bean
    public Client etcdClient(EtcdProperties etcdProperties) {
        return Client.builder().endpoints(etcdProperties.getEndpoint()).build();
    }

    @Bean
    public EtcdGenericSerializer etcdGenericSerializer() {
        return new EtcdGenericSerializer();
    }

    @Bean
    public EtcdJobStorage etcdJobStorage() {
        return new EtcdJobStorage();
    }
}
