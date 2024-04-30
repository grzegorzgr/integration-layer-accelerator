package com.kainos.tracing;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.ResourceUtils;

import brave.Tracing;
import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;

@Configuration
@EnableAspectJAutoProxy
@PropertySource(ResourceUtils.CLASSPATH_URL_PREFIX + "tracing.yml")
public class TracingConfiguration {
    public static final String TRACE_ID_BAGGAGE = "trace-id";
    public static final String IS_ASYNC_BAGGAGE = "is-async";
    public static final String ERROR_ID_BAGGAGE = "error-id";
    public static final String ERROR_CODE_BAGGAGE = "error-code";
    public static final String ERROR_MESSAGE_BAGGAGE = "error-message";

    public static final List<String> STANDARD_BAGGAGES_LIST = List.of(
        TRACE_ID_BAGGAGE,
        IS_ASYNC_BAGGAGE
    );

    @Bean
    BaggageField traceIdHeader() {
        return BaggageField.create(TRACE_ID_BAGGAGE);
    }

    @Bean
    BaggageField isAsyncBaggage() {
        return BaggageField.create(IS_ASYNC_BAGGAGE);
    }

    @Bean
    BaggageField errorIdBaggage() {
        return BaggageField.create(ERROR_ID_BAGGAGE);
    }

    @Bean
    BaggageField errorCodeBaggage() {
        return BaggageField.create(ERROR_CODE_BAGGAGE);
    }

    @Bean
    BaggageField errorMessageBaggage() {
        return BaggageField.create(ERROR_MESSAGE_BAGGAGE);
    }

    @Bean
    @Primary
    io.micrometer.tracing.Tracer tracer(CurrentTraceContext bridgeContext) {
        return new BraveTracer(tracing(braveCurrentTraceContext()).tracer(), bridgeContext, new BraveBaggageManager());
    }

    @Bean
    CurrentTraceContext bridgeContext(ThreadLocalCurrentTraceContext braveCurrentTraceContext) {
        return new BraveCurrentTraceContext(braveCurrentTraceContext);
    }

    @Bean
    ThreadLocalCurrentTraceContext braveCurrentTraceContext() {
        return ThreadLocalCurrentTraceContext.newBuilder()
            .addScopeDecorator(MDCScopeDecorator.newBuilder()
                .add(CorrelationScopeConfig.SingleCorrelationField.create(this.traceIdHeader()))
                .add(CorrelationScopeConfig.SingleCorrelationField.create(this.isAsyncBaggage()))

                .add(CorrelationScopeConfig.SingleCorrelationField.create(this.errorIdBaggage()))
                .add(CorrelationScopeConfig.SingleCorrelationField.create(this.errorCodeBaggage()))
                .add(CorrelationScopeConfig.SingleCorrelationField.create(this.errorMessageBaggage()))
                .build())
            .build();
    }

    @Bean
    Tracing tracing(ThreadLocalCurrentTraceContext braveCurrentTraceContext) {
        return Tracing.newBuilder()
            .currentTraceContext(braveCurrentTraceContext)
            .supportsJoin(true)
            .traceId128Bit(true)
            // For Baggage to work you need to provide a list of fields to propagate
            .propagationFactory(BaggagePropagation.newFactoryBuilder(B3Propagation.FACTORY)
                .add(BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create(TRACE_ID_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create(IS_ASYNC_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create(ERROR_ID_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create(ERROR_CODE_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create(ERROR_MESSAGE_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(TRACE_ID_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(IS_ASYNC_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(ERROR_ID_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(ERROR_CODE_BAGGAGE)))
                .add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(ERROR_MESSAGE_BAGGAGE)))
                .build())
            .sampler(Sampler.ALWAYS_SAMPLE)
            .build();
    }
}

