package com.kainos.errorhandling.monitoring.consumers;

import static java.nio.charset.Charset.defaultCharset;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.kainos.consumermanagement.stopping.ConsumerStoppedService;
import com.kainos.consumermanagement.stopping.StoppedConsumerEntry;
import com.kainos.consumermanagement.stopping.StoppedConsumerEntryKeyProvider;
import com.kainos.model.ConsumersResponse;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.ByteSequence;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ConsumersService {
    private static final String CONSUMER = "consumer";

    private final Client etcdClient;
    private final ConsumerStoppedService consumerStoppedService;

    List<ConsumersResponse> getAllPausedConsumersResponse() {
        return consumerStoppedService.getConsumerResponses(getAllStoppedConsumersEntries());
    }

    private List<StoppedConsumerEntry> getAllStoppedConsumersEntries() {
        return consumerStoppedService.getEtcdEntriesWithPrefix(StoppedConsumerEntryKeyProvider.CONSUMER_CONFIG_KEY);
    }

    /**
     * @param errorSource The name of the process that caused the consumers to stop consuming e.g. onboardings, deliveries;
     */
    void resumeConsumerByErrorSource(String errorSource) {
        removeStoppedConsumerEntryByConsumerIdentificator(errorSource);
    }

    void resumeAll() {
        removeStoppedConsumerEntryByConsumerIdentificator(CONSUMER);
    }

    private void removeStoppedConsumerEntryByConsumerIdentificator(String key) {
        CompletableFuture[] consumersFutures = filterConsumersByIdentificator(key)
            .map(stoppedConsumerEntry -> {
                log.info("Removing key {} from etcd.", key);
                return etcdClient.getKVClient()
                    .delete(ByteSequence.from(stoppedConsumerEntry.getConsumerIdentificator(), defaultCharset()));
            })
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(consumersFutures);
    }

    private Stream<StoppedConsumerEntry> filterConsumersByIdentificator(String value) {
        return getAllStoppedConsumersEntries()
            .stream()
            .filter(consumerEntry -> consumerEntry.getConsumerIdentificator().contains(value));
    }
}
