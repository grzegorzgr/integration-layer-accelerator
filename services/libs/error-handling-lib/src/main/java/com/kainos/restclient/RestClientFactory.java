package com.kainos.restclient;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import static com.kainos.mapper.ObjectMapperFactory.getNonNullIgnoreUnknownFieldsObjectMapper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.httpclient5.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient5.LogbookHttpResponseInterceptor;

import com.kainos.exception.ErrorResponseMessageConverter;
import com.kainos.properties.TimeoutProperties;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class RestClientFactory {

    //private Logbook logbook;
    private RestTemplateBuilder springRestTemplateBuilder;
    private TimeoutProperties timeoutProperties;

    public RestTemplate createRestTemplate(RestClientConfig config) throws GeneralSecurityException, IOException {
        validateRestTemplateConfig(config);
        Optional<SSLContext> sslContext = createSslContext(config);
        HttpClient httpClient = createHttpClient(config.isUseInternalTimeoutProperties(), sslContext);

        RestTemplate restTemplate = springRestTemplateBuilder
            .requestFactory(() -> new BufferingClientHttpRequestFactory(getRequestFactory(httpClient, config)))
            .messageConverters(getMessageConverters(config))
            .additionalMessageConverters(new ErrorResponseMessageConverter())
            .build();

        if (config.getServiceUrl() != null) {
            restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(config.getServiceUrl()));
        }

        return restTemplate;
    }

    private TimeoutableHttpComponentsClientHttpRequestFactory getRequestFactory(HttpClient httpClient, RestClientConfig config) {
        return config.isUseInternalTimeoutProperties()
            ? new TimeoutableHttpComponentsClientHttpRequestFactory(httpClient,
            timeoutProperties.getInternalConnectTimeoutMs(),
            timeoutProperties.getInternalConnectionRequestTimeoutMs())
            : new TimeoutableHttpComponentsClientHttpRequestFactory(httpClient,
            timeoutProperties.getConnectTimeoutMs(),
            timeoutProperties.getConnectionRequestTimeoutMs());
    }

    private Optional<SSLContext> createSslContext(RestClientConfig config) throws GeneralSecurityException, IOException {
        if (isTrustStoreConfigured(config) || config.isIgnoreBadServerCertificate() || isClientCertConfigured(config)) {
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
            if (config.isIgnoreBadServerCertificate()) {
                sslContextBuilder = sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            } else if (isTrustStoreConfigured(config)) {
                sslContextBuilder = sslContextBuilder.loadTrustMaterial(
                    config.getTrustStore(),
                    config.getTrustStorePassword().toCharArray());
            }

            if (isClientCertConfigured(config)) {
                sslContextBuilder = sslContextBuilder.loadKeyMaterial(
                    config.getKeyStore(),
                    config.getKeyStorePassword().toCharArray(),
                    config.getKeyPassword().toCharArray()
                );
            }
            return Optional.of(sslContextBuilder.build());
        } else {
            return Optional.empty();
        }
    }

    private boolean isClientCertConfigured(RestClientConfig config) {
        return config.getKeyStore() != null && config.getKeyPassword() != null && config.getKeyStorePassword() != null;
    }

    private boolean isTrustStoreConfigured(RestClientConfig config) {
        return config.getTrustStore() != null && config.getTrustStorePassword() != null;
    }

    private HttpClient createHttpClient(boolean useInternalTimeoutProperties, Optional<SSLContext> sslContext) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .disableAutomaticRetries()
            .addRequestInterceptorFirst(new LogbookHttpRequestInterceptor(Logbook.create()))
            .addResponseInterceptorLast(new LogbookHttpResponseInterceptor());

        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(useInternalTimeoutProperties
                    ? timeoutProperties.getInternalReadTimeoutMs()
                    : timeoutProperties.getReadTimeoutMs()))
                .build());

        sslContext.ifPresent(sslContext1 -> {
            var sslConnectionSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext.get())
                .setHostnameVerifier(new NoopHostnameVerifier())
                .build();

            connectionManagerBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
        });

        httpClientBuilder.setConnectionManager(connectionManagerBuilder.build());

        return httpClientBuilder.build();
    }

    private void validateRestTemplateConfig(RestClientConfig config) {
        List<Object> allNullOrAllNotNull = Arrays.asList(config.getKeyPassword(), config.getKeyStorePassword(), config.getKeyStore());
        long nullProperties = allNullOrAllNotNull
            .stream()
            .filter(Objects::nonNull)
            .count();
        if (nullProperties != 0 && nullProperties != allNullOrAllNotNull.size()) {
            throw new IllegalArgumentException("keyPassword, keyStorePassword, keyStore - all those properties should be null or set");
        }
    }

    private List<HttpMessageConverter<?>> getMessageConverters(RestClientConfig config) {
        if (isNotEmpty(config.getCustomMessageConverters())) {
            return config.getCustomMessageConverters();
        } else {
            List<HttpMessageConverter<?>> defaultMessageConverters = new RestTemplate().getMessageConverters();
            replaceJsonConverterWithNonNullIgnoreUnknownFieldsObjectMapper(defaultMessageConverters);
            return defaultMessageConverters;
        }
    }

    private void replaceJsonConverterWithNonNullIgnoreUnknownFieldsObjectMapper(List<HttpMessageConverter<?>> defaultMessageConverters) {
        defaultMessageConverters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        defaultMessageConverters.add(new MappingJackson2HttpMessageConverter(getNonNullIgnoreUnknownFieldsObjectMapper()));
    }
}
