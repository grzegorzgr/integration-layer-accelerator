package sfdc.client;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SfdcClientAuthInterceptor implements ClientHttpRequestInterceptor {
    private static final int RETRY_COUNT = 3;
    private static final long BACKOFF_PERIOD_MS = 5000;

    private final RetryTemplate retryTemplate;
    private final TokenService tokenService;

    SfdcClientAuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
        this.retryTemplate = new RetryTemplate();

        var fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(BACKOFF_PERIOD_MS);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        var retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(RETRY_COUNT);
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        return retryTemplate.execute(retryContext -> {
            var token = tokenService.retrieveToken();
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            ClientHttpResponse result = null;

            try {
                result = execution.execute(request, body);
            } catch (IOException exception) {
                log.error("Unhandled IOException occurred in retry block. Skipping retry", exception);
                retryContext.setExhaustedOnly();
                throw exception;
            }

            if (result != null && result.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                //clear token so on the next retry the retrieveToken is called again
                try {
                    tokenService.clearToken();
                    log.warn("SFDC return 401 for authorization, retrying {} more times", RETRY_COUNT - retryContext.getRetryCount());
                    throw new RuntimeException("SFDC return 401 for authorization, retrying "
                        + (RETRY_COUNT - retryContext.getRetryCount()) + " more times");
                } finally {
                    result.close();
                }
            } else {
                return result;
            }
        });
    }
}
