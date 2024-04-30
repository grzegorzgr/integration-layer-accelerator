package com.kainos.etcd.jobstorage.helpers;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kainos.etcd.jobstorage.model.EtcdJob;

public class EtcdGenericSerializer {

    @Autowired
    private ObjectMapper objectMapper;

    public String serialize(EtcdJob etcdJob) {
        try {
            return objectMapper.writeValueAsString(etcdJob);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize EtcdJob");
        }
    }

    public <T> EtcdJob<T> deserializeEtcdJob(String serialized, Class<T> dataType) {
        // This is how you deserialize EtcdJob<T> where T is provided at runtime (as dataType method parameter)
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(EtcdJob.class, dataType);

        try {
            return objectMapper.readValue(serialized, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot deserialize EtcdJob", e);
        }
    }
}
