package com.kainos;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The logging system is initialized early in the application lifecycle
 * and as such logging properties will not be found in property files
 * loaded via @PropertySource annotations.
 */
@Slf4j
public class ErrorHandlingPropertiesConfiguration implements EnvironmentPostProcessor {

    private static final String RESOURCE_LOCATION = ResourceUtils.CLASSPATH_URL_PREFIX + "errorhandling.properties";
    private static final String ERROR_HANDLIG_PREFIX = "errorhandling";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File file;
        Map<String, Object> envMap = new HashMap<>();
        try {
            file = ResourceUtils.getFile(RESOURCE_LOCATION);
            Properties properties = loadProperties(file);
            properties.forEach((propKey, propValue) -> envMap.put((String) propKey, propValue));
        } catch (Exception e) {
            log.warn("Resource {} cannot be resolved to a file in the file system", RESOURCE_LOCATION);
        }

        environment.getPropertySources()
            .addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MapPropertySource(ERROR_HANDLIG_PREFIX, envMap));
    }

    private Properties loadProperties(File f) {
        FileSystemResource resource = new FileSystemResource(f);
        try {
            return PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load local settings from " + f.getAbsolutePath(), ex);
        }
    }
}
