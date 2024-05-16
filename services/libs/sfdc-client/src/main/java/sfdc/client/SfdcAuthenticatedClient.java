package sfdc.client;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static com.kainos.mapper.ObjectMapperFactory.getNonNullObjectMapper;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kainos.exception.UnknownException;
import com.sfdc.account.model.Account;

import lombok.extern.slf4j.Slf4j;
import sfdc.properties.SfdcAuthenticatedProperties;
import sfdc.wrapper.SfdcErrorHandlingRestTemplateWrapper;

@Component
@Slf4j
public class SfdcAuthenticatedClient {
    private static final String NEW_ACCOUNT_ENDPOINT = "/services/account";

    @Autowired
    @Qualifier("sfdcAuthenticatedRestTemplateWrapper")
    private SfdcErrorHandlingRestTemplateWrapper sfdcAuthenticatedRestTemplateWrapper;

    @Autowired
    private SfdcAuthenticatedProperties sfdcAuthenticatedProperties;

    public void createNewAccount(Account account) {
        exchange(NEW_ACCOUNT_ENDPOINT, POST, account);
    }

    private void exchange(String path, HttpMethod httpMethod, Object body) {
        exchange(path, httpMethod, body, Void.class);
    }

    private <T> ResponseEntity<T> exchange(String path, HttpMethod httpMethod, Object body, Class<T> responseType) {
        return exchange(path, httpMethod, body, responseType, false);
    }

    private <T> ResponseEntity<T> exchange(String path, HttpMethod httpMethod, Object body, Class<T> responseType, boolean rawBody) {
        if (!rawBody && httpMethod.equals(PATCH)) {
            body = serializeBodyWithPreconfiguredMapper(body, getNonNullObjectMapper());
        }

        return sfdcAuthenticatedRestTemplateWrapper.exchange(getUri(path), httpMethod, createHttpEntity(body), responseType);
    }

    private String serializeBodyWithPreconfiguredMapper(Object body, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw UnknownException.builder()
                .message("Error during JSON serialization")
                .cause(e)
                .build();
        }
    }

    private URI getUri(String path) {
        return UriComponentsBuilder
            .fromUriString(sfdcAuthenticatedProperties.getHost())
            .path(path)
            .build()
            .toUri();
    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        return new HttpEntity<>(body, headers);
    }
}
