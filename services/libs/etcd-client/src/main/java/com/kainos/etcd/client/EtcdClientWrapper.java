package com.kainos.etcd.client;

import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import lombok.extern.slf4j.Slf4j;

/**
 * Etcd is a distributed reliable key-value store for the most critical data of a distributed system.
 * EtcdClientWrapper is a closed abstraction that can receive any object, serialize and write to the store
 * or read from the store and deserialize to any object.
 */
@Slf4j
public class EtcdClientWrapper {

    @Autowired
    private Client etcdClient;

    @Autowired
    private ObjectMapper objectMapper;

    public <T> void put(String key, T value) {
        try {
            String serializedValue = objectMapper.writeValueAsString(value);
            log.info("Writing Etcd Entry, key {}, value {}", key, serializedValue);
            etcdClient.getKVClient().put(
                ByteSequence.from(key, defaultCharset()),
                ByteSequence.from(serializedValue, defaultCharset())).get();
        } catch (Exception e) {
            throw new RuntimeException("Error during propagating key-value pair into etcd with key: " + key, e);
        }
    }

    public <T> List<T> getValuesBasedOnKeyPrefix(String prefix, Class<T> valueType) {
        ByteSequence byteSequenceFromPrefix = ByteSequence.from(prefix, defaultCharset());
        try {
            return etcdClient.getKVClient().get(
                    byteSequenceFromPrefix,
                    GetOption.newBuilder().withPrefix(byteSequenceFromPrefix).build())
                .get()
                .getKvs()
                .stream()
                .map(keyValue -> readValue(keyValue, valueType))
                .collect(Collectors.toList());
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error during reading entries from etcd", e);
        }
    }

    private <T> T readValue(KeyValue keyValue, Class<T> valueType) {
        try {
            return objectMapper.readValue(keyValue.getValue().toString(defaultCharset()), valueType);
        } catch (IOException e) {
            throw new RuntimeException("Error during deserializing reprocess request object.", e);
        }
    }

    public Optional<String> getSingleEntryValue(String key) {
        ByteSequence byteSequenceFromPrefix = ByteSequence.from(key, defaultCharset());

        try {
            return etcdClient.getKVClient().get(byteSequenceFromPrefix).get().getKvs().stream()
                .map((keyValue -> keyValue.getValue().toString(defaultCharset())))
                .findFirst()
                .filter(value -> !"null".equals(value));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error during reading from etcd. Key name: " + key, e);
        }
    }

    public void delete(String key) {
        try {
            etcdClient.getKVClient().delete(ByteSequence.from(key, defaultCharset())).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error during removing entry from etcd.", e);
        }
    }
}
