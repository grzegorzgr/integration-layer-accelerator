package sfdc.client;

import static com.kainos.mapper.ObjectMapperFactory.getNonEmptyIgnoreUnknownFieldsWithObjectMapper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.kainos.restclient.RestClientConfig;
import com.kainos.restclient.RestClientFactory;
import com.kainos.tracing.TraceContextProvider;

import sfdc.properties.SfdcAuthenticatedProperties;
import sfdc.wrapper.SfdcErrorHandlingRestTemplateWrapper;

@Configuration
public class SFDCClientConfiguration {

    @Autowired
    private RestClientFactory restClientFactory;

    @Autowired
    private SfdcAuthenticatedProperties sfdcAuthenticatedProperties;

    @Bean("plainRestTemplate")
    @Primary
    public RestTemplate plainRestTemplate(RestClientFactory restClientFactory) throws GeneralSecurityException, IOException {
        return restClientFactory.createRestTemplate(
            RestClientConfig.builder().build());
    }

    @Bean
    public TokenService tokenService() {
        return new TokenService();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TokenStorage tokenStorage() {
        return new TokenStorage();
    }

    @Bean
    @Qualifier("sfdcAuthenticatedRestTemplate")
    public RestTemplate sfdcAuthenticatedRestTemplate(TokenService tokenService
    ) throws GeneralSecurityException, IOException {
        RestTemplate restTemplate = restClientFactory.createRestTemplate(getRestClientConfig());

        restTemplate.getInterceptors().add(new SfdcClientAuthInterceptor(tokenService));

        return restTemplate;
    }

    @Bean
    public SfdcErrorHandlingRestTemplateWrapper sfdcAuthenticatedRestTemplateWrapper(
        @Qualifier("sfdcAuthenticatedRestTemplate") RestTemplate restTemplate, TraceContextProvider traceContextProvider) {

        return new SfdcErrorHandlingRestTemplateWrapper(restTemplate, traceContextProvider);
    }

    private RestClientConfig getRestClientConfig() {
        return RestClientConfig.builder()
            .serviceUrl(sfdcAuthenticatedProperties.getHost())
            .ignoreBadServerCertificate(true)
            .customMessageConverters(List.of(
                new StringHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(getNonEmptyIgnoreUnknownFieldsWithObjectMapper()),
                new FormHttpMessageConverter()
            ))
            .build();
    }
}
