package com.kainos.consumermanagement.stopping;

import static java.nio.charset.Charset.defaultCharset;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.kainos.model.ConsumersResponse;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.options.GetOption;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(value = "etcd.endpoint")
public class ConsumerStoppedService {
    private static final String ERROR_DURING_READING_ENTRIES_FROM_ETCD = "Error during reading entries from etcd";

    private final Client etcdClient;
    private final StoppedConsumerEntryKeyProvider stoppedConsumerEntryKeyProvider;
    private final KafkaListenerEndpointRegistry registry;

    @Autowired
    public ConsumerStoppedService(
        Client etcdClient,
        KafkaListenerEndpointRegistry registry,
        StoppedConsumerEntryKeyProvider stoppedConsumerEntryKeyProvider) {

        this.etcdClient = etcdClient;
        this.stoppedConsumerEntryKeyProvider = stoppedConsumerEntryKeyProvider;
        this.registry = registry;
    }

    public List<ConsumersResponse> getStoppedConsumersForCurrentApplication() {
        return getConsumersResponseByPredicate(getApplicationConsumers(), this::isContainerPaused);
    }

    public List<ConsumersResponse> getConsumerResponses(List<StoppedConsumerEntry> consumerList) {
        return getConsumersResponseByPredicate(consumerList, listenerId -> true);
    }

    public List<ConsumersResponse> getConsumersResponseByPredicate(List<StoppedConsumerEntry> consumerList,
        Predicate<String> listenerIdPredicate) {
        return consumerList
            .stream()
            .flatMap(stoppedConsumerEntry -> {
                var listenerId = getListenerIdFromStoppedConsumerEntry(stoppedConsumerEntry);
                if (listenerIdPredicate.test(listenerId)) {
                    return Stream.of(
                        ConsumersResponse
                            .builder()
                            .consumerName(listenerId)
                            .sourceSystems(List.copyOf(prettifySourceSystem(stoppedConsumerEntry.getStoppingReason())))
                            .build()
                    );
                } else {
                    return Stream.empty();
                }
            })
            .collect(Collectors.toList());
    }

    private boolean isContainerPaused(String listenerId) {
        var container = registry.getListenerContainer(stoppedConsumerEntryKeyProvider.withoutApplicationKey(listenerId));
        if (container == null) {
            return false;
        }
        return container.isContainerPaused() || !container.isRunning();
    }

    /**
     * @param consumerName consumer name e.g. "deliveries-propagation"
     * @param errorSource the source system of the error f.e. exact, css, sfdc, windream.
     * Error source is equal manual if consumer is stopped manually using our POST endpoint /consumers/{consumer}
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void pauseConsumerWithId(String consumerName, String errorSource) throws ExecutionException, InterruptedException {
        var key = stoppedConsumerEntryKeyProvider.consumerConfigKey(consumerName);
        log.info("Sending PAUSE signal to ETCD for consumers: {}", key);
        var value = etcdClient.getKVClient().get(ByteSequence.from(key, defaultCharset()))
            .get()
            .getKvs()
            .stream()
            .map(kv -> decode(kv.getValue()))
            .findFirst()
            .map(current -> {
                current.add(errorSource);
                return current;
            })
            .orElse(Sets.newHashSet(errorSource));

        etcdClient.getKVClient().put(
            ByteSequence.from(key, defaultCharset()),
            ByteSequence.from(encode(value), defaultCharset())
        ).get();
    }

    public void resumeConsumerWithId(String consumerName) throws ExecutionException, InterruptedException {
        log.info("Sending RESUME for consumers: {}", stoppedConsumerEntryKeyProvider.consumerConfigKey(consumerName));
        etcdClient.getKVClient().delete(
                ByteSequence.from(stoppedConsumerEntryKeyProvider.consumerConfigKey(consumerName), defaultCharset()))
            .get();
    }

    void resumeAllConsumersForCurrentService() {
        log.info("Sending RESUME for all consumers in this service");
        var stoppedConsumers = getEtcdEntriesWithPrefix(stoppedConsumerEntryKeyProvider.consumerConfigKey());
        stoppedConsumers.stream().map(stoppedConsumerEntry -> {
            log.info("Removing key {} from etcd.", stoppedConsumerEntry.getConsumerIdentificator());
            return etcdClient.getKVClient().delete(ByteSequence.from(stoppedConsumerEntry.getConsumerIdentificator(), defaultCharset()));
        }).collect(Collectors.toList());
    }

    private static String encode(HashSet<String> value) {
        return String.join(";", value);
    }

    private HashSet<String> prettifySourceSystem(String value) {
        return Sets.newHashSet(value.split(";"));
    }

    private HashSet<String> decode(ByteSequence value) {
        return Sets.newHashSet(value.toString(defaultCharset()).split(";"));
    }

    private String getListenerIdFromStoppedConsumerEntry(StoppedConsumerEntry stoppedConsumerEntry) {
        String consumerIdentificator = stoppedConsumerEntry.getConsumerIdentificator();
        return consumerIdentificator.substring(consumerIdentificator.indexOf('.') + 1);
    }

    private List<StoppedConsumerEntry> getApplicationConsumers() {
        return getEtcdEntriesWithPrefix(stoppedConsumerEntryKeyProvider.consumerConfigKey());
    }

    public List<StoppedConsumerEntry> getEtcdEntriesWithPrefix(String keyProvider) {
        ByteSequence byteSequenceFromPrefix = ByteSequence.from(keyProvider, defaultCharset());
        try {
            return etcdClient.getKVClient().get(
                    byteSequenceFromPrefix,
                    GetOption.newBuilder().withPrefix(byteSequenceFromPrefix).build())
                .get()
                .getKvs()
                .stream()
                .map(StoppedConsumerEntry::new)
                .collect(Collectors.toList());
        } catch (ExecutionException e) {
            throw new RuntimeException(ERROR_DURING_READING_ENTRIES_FROM_ETCD, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ERROR_DURING_READING_ENTRIES_FROM_ETCD, e);
        }
    }
}
