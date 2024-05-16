package sfdc.properties;

import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Component
@Data
@ConfigurationProperties("sfdc.authenticated")
@Validated
public class SfdcAuthenticatedProperties {
    @NotNull
    private String host;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String clientId;
    @NotNull
    private String secret;
}
