package com.kainos.logging.sensitivedata.redactor.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

//@Primary
//@Component
@Data
@Validated
@EnableConfigurationProperties
@ConfigurationProperties("global.redactor")
public class GlobalRedactorProperties {

    @NotNull
    private Boolean enabled;
}
