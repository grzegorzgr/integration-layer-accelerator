package com.kainos.etcd.jobstorage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtcdJob<T> {
    private String fullKey;
    private String businessKey;
    private String traceId;
    private T data;
}

