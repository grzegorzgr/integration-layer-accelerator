package com.kainos.kafka.ssl;

import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

@Component
public class SslPropertiesProvider {

    @Value("${spring.kafka.ssl.key-store-location:}")
    private String keyStoreLocation;
    @Value("${spring.kafka.ssl.trust-store-location:}")
    private String trustStoreLocation;

    private File tempKeystore;
    private File tempTruststore;

    public Map<String, Object> setSslCertsLocation(Map<String, Object> properties) throws IOException {
        if (keyStoreLocation.isEmpty() && trustStoreLocation.isEmpty()) {
            if (tempKeystore == null) {
                tempKeystore = File.createTempFile("keystore", ".jks");
                copy("dev.kafkaclient.keystore.jks", tempKeystore);

                tempTruststore = File.createTempFile("truststore", ".jks");
                copy("dev.kafkaclient.truststore.jks", tempTruststore);
            }
            properties.put(SSL_KEYSTORE_LOCATION_CONFIG, tempKeystore.getAbsolutePath());
            properties.put(SSL_TRUSTSTORE_LOCATION_CONFIG, tempTruststore.getAbsolutePath());
        } else if (keyStoreLocation.isEmpty() || trustStoreLocation.isEmpty()) {
            throw new InvalidParameterException("Only one of keystore/truststore is set.");
        }

        properties.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        return properties;
    }

    private void copy(String inputFilePath, File outputFile) throws IOException {
        FileCopyUtils.copy(new ClassPathResource(inputFilePath).getInputStream(),
            new FileOutputStream(outputFile));
    }
}
