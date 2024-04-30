package com.kainos.kafka.redactor;

import java.util.Map;

import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kainos.logging.sensitivedata.redactor.JsonRedactor;

@Component
public class RedactorPropertiesProvider {
    private static final String JSON_REDACTOR_BEAN = WordUtils.uncapitalize(JsonRedactor.class.getSimpleName()) + ".bean";

    @Autowired
    private JsonRedactor jsonRedactor;

    public Map<String, Object> setJsonRedactor(Map<String, Object> properties) {
        properties.put(JSON_REDACTOR_BEAN, jsonRedactor);
        return properties;
    }
}

