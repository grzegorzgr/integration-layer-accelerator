package com.kainos.logging.sensitivedata.redactor.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("jsonredact")
@EnableConfigurationProperties
public class JsonRedactorProperties extends RedactorProperties {
    private List<String> redlist = List.of(
        ".*name.*",
        ".*iban.*",
        ".*phone.*",
        ".*email.*",
        ".*city.*",
        ".*street.*"
    );

    private List<String> greenlist = List.of(
        ".*key.*",
        ".*id.*"
    );

    private List<String> attributeDataTypeRedlist = List.of(
        "phone",
        "email"
    );

    private Boolean enabled = true;
}
