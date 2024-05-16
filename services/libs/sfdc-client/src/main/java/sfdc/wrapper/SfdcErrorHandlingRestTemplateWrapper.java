package sfdc.wrapper;

import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.util.StreamUtils.copy;

import static com.kainos.tracing.TracingConfiguration.TRACE_ID_BAGGAGE;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.kainos.exception.BusinessException;
import com.kainos.exception.TechnicalException;
import com.kainos.exception.UnknownException;
import com.sfdc.model.SalesforceCompositeResponse;
import com.kainos.tracing.TraceContextProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SfdcErrorHandlingRestTemplateWrapper {
    private static final List<HttpStatus> BUSINESS_ERROR_STATUS_CODES = List.of(
        HttpStatus.BAD_REQUEST,
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        HttpStatus.INTERNAL_SERVER_ERROR,
        HttpStatus.NOT_FOUND);

    private final RestTemplate restTemplate;
    private final TraceContextProvider traceContextProvider;

    public SfdcErrorHandlingRestTemplateWrapper(RestTemplate restTemplate, TraceContextProvider traceContextProvider) {
        this.traceContextProvider = traceContextProvider;
        this.restTemplate = restTemplate;
    }

    public void executeGetToFile(URI uri, File tmpFile) {
        try {
            restTemplate.execute(uri, GET, request -> request.getHeaders().set(TRACE_ID_BAGGAGE, traceContextProvider.traceId()),
                clientHttpResponse -> copy(clientHttpResponse.getBody(), openOutputStream(tmpFile)));
        } catch (HttpStatusCodeException e) {
            handleHttpStatusCodeException(e, false);
        } catch (ResourceAccessException e) {
            throw new TechnicalException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UnknownException(e.getMessage(), e);
        }
    }

    public <T> ResponseEntity<T> exchange(URI uri, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
        try {
            ResponseEntity<T> responseEntity = restTemplate.exchange(uri, method, requestEntity, responseType);
            return processResponseEntity(responseEntity, responseType);
        } catch (HttpStatusCodeException e) {
            handleHttpStatusCodeException(e, true);
        } catch (ResourceAccessException e) {
            throw new TechnicalException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UnknownException(e.getMessage(), e);
        }

        return null;
    }

    public <T> ResponseEntity<T> exchange(URI uri, HttpMethod method, HttpEntity<?> requestEntity,
        ParameterizedTypeReference<T> responseType) {
        try {
            return restTemplate.exchange(uri, method, requestEntity, responseType);
        } catch (HttpStatusCodeException e) {
            handleHttpStatusCodeException(e, true);
        } catch (ResourceAccessException e) {
            throw new TechnicalException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UnknownException(e.getMessage(), e);
        }

        return null;
    }

    private void handleHttpStatusCodeException(HttpStatusCodeException e, boolean allowForConflict) {
        if (allowForConflict && CONFLICT.equals(e.getStatusCode())) {
            log.info("HTTP Conflict response received from SFDC but not treated as an error");
            return;
        }

        String message = e.getMessage();

        if (BUSINESS_ERROR_STATUS_CODES.contains(e.getStatusCode())) {
            throw new BusinessException(message, e);
        } else if (e.getStatusCode().is5xxServerError()) {
            throw new TechnicalException(message, e);
        } else {
            throw new UnknownException(message, e);
        }
    }

    private <T> ResponseEntity<T> processResponseEntity(ResponseEntity<T> responseEntity, Class<T> responseType) {
        if (responseType == SalesforceCompositeResponse.class) {
            verifyCompositeResponse(responseEntity);
        }
        return responseEntity;
    }

    private <T> void verifyCompositeResponse(ResponseEntity<T> responseEntity) {
        SalesforceCompositeResponse compositeResponse = ((SalesforceCompositeResponse) responseEntity.getBody());
        if (compositeResponse != null) {
            compositeResponse.getCompositeResponse().forEach(response -> {
                HttpStatus status = HttpStatus.valueOf(response.getHttpStatusCode());
                if (status.isError()) {
                    throw new SfdcHttpStatusCodeException(status, response.getBody().toString(),
                        String.valueOf(response.getBody()).getBytes(StandardCharsets.UTF_8));
                }
            });
        } else {
            throw new IllegalStateException("No composite response body");
        }
    }

    private static class SfdcHttpStatusCodeException extends HttpStatusCodeException {
        SfdcHttpStatusCodeException(HttpStatus statusCode, String statusText, byte[] responseBody) {
            super(statusCode, statusText, responseBody, StandardCharsets.UTF_8);
        }
    }
}
