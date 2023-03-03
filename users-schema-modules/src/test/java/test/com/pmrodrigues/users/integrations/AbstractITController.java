package test.com.pmrodrigues.users.integrations;

import lombok.NonNull;
import lombok.val;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestTemplate;

public class AbstractITController {
    protected String API_URL = "http://localhost:8143";
    protected RestTemplate rest;
    @Value("${KEYCLOAK_LOCATION:http://localhost:8080/auth}")
    private String SERVER_URL;
    @Value("${KEYCLOAK_REALM:master}")
    private String REALM;
    @Value("${KEYCLOAK_CLIENT_ID:94cf4fee-1b57-4e3c-8d97-195e7f7f1173}")
    private String CLIENT_ID;
    @Value("${KEYCLOAK_CLIENT_SECRET:gNjirmWqaUiP4NWREgRDpbzJpnq7WSZD}")
    private String CLIENT_SECRET;

    public RestTemplate generateToken(@NonNull String username, @NonNull String password) {
        val keycloak = KeycloakBuilder
                .builder()
                .serverUrl(SERVER_URL)
                .realm(REALM)
                .username(username)
                .password(password)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .grantType(AuthorizationGrantType.PASSWORD.getValue())
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();


        val token = keycloak.tokenManager().getAccessTokenString();
        return new RestTemplateBuilder().rootUri(API_URL)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }
}
