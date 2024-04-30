package com.kainos.consumermanagement.configwatcher;

import static java.nio.charset.Charset.defaultCharset;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.kainos.consumermanagement.stopping.StoppedConsumerEntryKeyProvider;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.options.WatchOption;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(value = "etcd.endpoint")
public class ConfigChangeWatcher {

    private final Executor executor;
    private final Client etcdClient;
    private final ConfigChangeWatcherListener configChangeWatcherListener;
    private final StoppedConsumerEntryKeyProvider stoppedConsumerEntryKeyProvider;

    public ConfigChangeWatcher(@Qualifier("singleThreadExecutor") Executor executor,
        Client etcdClient,
        ConfigChangeWatcherListener configChangeWatcherListener,
        StoppedConsumerEntryKeyProvider stoppedConsumerEntryKeyProvider) {
        this.executor = executor;
        this.etcdClient = etcdClient;
        this.configChangeWatcherListener = configChangeWatcherListener;
        this.stoppedConsumerEntryKeyProvider = stoppedConsumerEntryKeyProvider;
    }

    @PostConstruct
    public void init() {
        executor.execute(this::startWatching);
    }

    @SuppressWarnings("squid:S2189")
    private void startWatching() {
        log.info("Starting config watcher");
        var key = ByteSequence.from(stoppedConsumerEntryKeyProvider.consumerConfigKey(), defaultCharset());
        var options = WatchOption.newBuilder()
            .withProgressNotify(true)
            .withPrefix(key)
            .build();

        etcdClient.getWatchClient().watch(
            key,
            options,
            configChangeWatcherListener::consume
        );
    }
}
