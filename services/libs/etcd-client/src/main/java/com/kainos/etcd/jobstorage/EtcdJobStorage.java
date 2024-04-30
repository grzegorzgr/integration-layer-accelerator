package com.kainos.etcd.jobstorage;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.kainos.etcd.client.EtcdClientWrapper;
import com.kainos.etcd.jobstorage.helpers.EtcdGenericSerializer;
import com.kainos.etcd.jobstorage.model.EtcdJob;
import com.kainos.tracing.TraceContextProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EtcdJobStorage {
    private static final String ETCDJOB_PREFIX = "etcdjob";

    @Autowired
    private EtcdClientWrapper etcdClientWrapper;

    @Autowired
    private TraceContextProvider traceContextProvider;

    @Autowired
    private EtcdGenericSerializer etcdGenericSerializer;

    public <T> void saveJob(String businessKey, T data) {
        log.info("Saving new EtcdJob {} with key {}", businessKey);

        String fullKey = getFullKey(businessKey);

        EtcdJob etcdJob = EtcdJob.<T>builder()
            .businessKey(businessKey)
            .fullKey(fullKey)
            .traceId(traceContextProvider.traceId())
            .data(data)
            .build();

        String serializedEtcdJob = etcdGenericSerializer.serialize(etcdJob);

        etcdClientWrapper.put(fullKey, serializedEtcdJob);
    }

    public <T> List<EtcdJob<T>> getSavedJobs(Class<T> dataClass) {
        List<String> serializedJobs = etcdClientWrapper.getValuesBasedOnKeyPrefix(
            getJobUniqueKeyPrefix(), String.class);

        List<EtcdJob<T>> jobs = serializedJobs.stream()
            .map(serialized -> etcdGenericSerializer.deserializeEtcdJob(serialized, dataClass))
            .collect(Collectors.toList());

        List<String> keys = jobs.stream().map(EtcdJob::getFullKey).collect(Collectors.toList());

        log.info("Fetched {} jobs from Etcd Job Storage for keys: {}", jobs.size(), keys);

        return jobs;
    }

    public void startJobAndAnnotateContext(EtcdJob etcdJob) {
        log.info("Starting processing of EtcdJob {} and annotating trace context", etcdJob.getFullKey());

        if (etcdJob.getTraceId() != null) {
            traceContextProvider.setTraceId(etcdJob.getTraceId());
        }
    }

    public void removeJob(EtcdJob etcdJob) {
        log.info("Removing EtcdJob, fullKey {}", etcdJob.getFullKey());
        etcdClientWrapper.delete(etcdJob.getFullKey());
    }

    private String getFullKey(String businessKey) {
        return getJobUniqueKeyPrefix() + businessKey;
    }

    private String getJobUniqueKeyPrefix() {
        return ETCDJOB_PREFIX + ".";
    }
}
