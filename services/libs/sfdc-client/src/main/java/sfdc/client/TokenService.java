package sfdc.client;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.sfdc.model.OAuthTokenResponse;

import sfdc.properties.SfdcAuthenticatedProperties;

public class TokenService {

    @Autowired
    private SfdcAuthenticatedProperties properties;

    @Autowired
    private TokenStorage tokenStorage;

    @Autowired
    @Qualifier("plainRestTemplate")
    private RestTemplate plainRestTemplate;

    void clearToken() {
        tokenStorage.setToken(null);
    }

    public String retrieveToken() throws IOException {
        String token = tokenStorage.getToken();
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        URI url = UriComponentsBuilder
            .fromUriString(properties.getHost())
            .path("/services/oauth2/token")
            .build()
            .toUri();

        LinkedMultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("grant_type", "password");
        formParams.add("username", properties.getUsername());
        formParams.add("password", properties.getPassword());
        formParams.add("client_id", properties.getClientId());
        formParams.add("client_secret", properties.getSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParams, headers);

        OAuthTokenResponse response = plainRestTemplate.postForEntity(url, request, OAuthTokenResponse.class).getBody();

        if (response != null && response.getAccessToken() != null) {
            String authToken = response.getAccessToken();
            tokenStorage.setToken(authToken);
            return authToken;
        }
        throw new RuntimeException("SFDC auth failed due to: " + response);
    }
}
