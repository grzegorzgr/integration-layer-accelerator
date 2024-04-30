package com.kainos.kafka.headers;

import static java.nio.charset.StandardCharsets.UTF_8;

import static com.kainos.mapper.SafeMapper.nullSafe;
import static com.kainos.tracing.TracingConfiguration.TRACE_ID_BAGGAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kainos.tracing.TraceContextProvider;

@Component
public class KafkaRecordHeadersProvider {

    @Autowired
    private TraceContextProvider traceContextProvider;

    public List<Header> createHeaders() {
        List<Header> headers = new ArrayList<>();
        addHeader(TRACE_ID_BAGGAGE, () -> traceContextProvider.traceId().getBytes(UTF_8), headers);
//        addHeader(SUBFLOW_ID_BAGGAGE, () -> traceContextProvider.subflowId().getBytes(UTF_8), headers);
//        addHeader(FEATURE_BAGGAGE, () -> traceContextProvider.feature().getBytes(UTF_8), headers);
//        addHeader(BUSINESS_OBJECT_TYPE_BAGGAGE, () -> traceContextProvider.businessObjectType().getBytes(UTF_8), headers);
//        addHeader(BUSINESS_ID_BAGGAGE, () -> traceContextProvider.businessId().getBytes(UTF_8), headers);
//        addHeader(BP_ROOT_ID_BAGGAGE, () -> traceContextProvider.bpRootId().getBytes(UTF_8), headers);
        return headers;
    }

    private void addHeader(String headerName, Supplier<byte[]> headerValueSupplier, List<Header> headers) {
        headers.add(new RecordHeader(headerName, nullSafe(headerValueSupplier)));
    }
}

