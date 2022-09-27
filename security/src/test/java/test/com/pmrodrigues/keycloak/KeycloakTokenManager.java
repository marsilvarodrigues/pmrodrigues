package test.com.pmrodrigues.keycloak;

import lombok.Builder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Builder
public class KeycloakTokenManager {

    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String realm;

    public RestTemplate oauth2RestTemplate() {

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8080/auth")
                .password(password)
                .username(username)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.PASSWORD)
                .build();


        TokenManager tokenmanager = keycloak.tokenManager();
        ClientHttpRequestInterceptor clientHttpRequestInterceptor = (request, body, execution) -> {
            AccessTokenResponse accessToken1 = tokenmanager.getAccessToken(); //only refreshes token when necessary
            String accessToken = accessToken1.getToken(); //get token as String
            HttpHeaders headers = request.getHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            return execution.execute(request, body);
        };
        RestTemplate template = new RestTemplate();
        template.setInterceptors(List.of(clientHttpRequestInterceptor));
        return template;
    }
}
