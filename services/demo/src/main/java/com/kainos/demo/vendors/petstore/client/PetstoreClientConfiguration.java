package com.kainos.demo.vendors.petstore.client;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.kainos.tracing.TraceIdFeignClientRequestInterceptor;

import feign.Client;
import feign.RequestInterceptor;
import feign.httpclient.ApacheHttpClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class PetstoreClientConfiguration {

    @Autowired
    private TraceIdFeignClientRequestInterceptor traceIdFeignClientRequestInterceptor;

    private static final String PKCS12 = "PKCS12";

    @Value("${petstore.sslConfig.keystore:#{null}}")
    private Resource keyStore;

    @Value("${petstore.sslConfig.keystorePassword:#{null}}")
    private String keystorePassword;

    @Bean
    public Client feignClient() {
        return new ApacheHttpClient(
            isSslEnabled() ? getCustomHttpClient() : HttpClients.createDefault());
    }

    @Bean
    public RequestInterceptor traceIdInterceptor() {
        return traceIdFeignClientRequestInterceptor.tracingInterceptor();
    }

    private CloseableHttpClient getCustomHttpClient() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(new TrustAllStrategy())
                .loadKeyMaterial(loadPkcs12KeyStore(), keystorePassword.toCharArray())
                .build();
            return HttpClientBuilder.create()
                .useSystemProperties()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Couldn't load ssl context", e.getCause());
        }
    }

    private KeyStore loadPkcs12KeyStore() {
        try {
            KeyStore keyStorePkcs12 = KeyStore.getInstance(PKCS12);
            keyStorePkcs12.load(keyStore.getInputStream(), keystorePassword.toCharArray());
            log.trace("Key store has " + keyStorePkcs12.size() + " keys");
            return keyStorePkcs12;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Couldn't load keystore from configuration", e.getCause());
        }
    }

    private boolean isSslEnabled() {
        return keyStore != null && keystorePassword != null;
    }
}