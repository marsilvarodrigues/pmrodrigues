package test.com.pmrodrigues.users.bdd;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
abstract class AbstractIntegrationService {
    protected String API_URL = "http://localhost:8143";

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
        val rest = new RestTemplateBuilder().rootUri(API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .interceptors((request, body, execution) -> {
                    log.info("request headers {} and body {}", request.getHeaders() , new String(body));
                    return execution.execute(request, body);
                })
                .build();




        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory() {
            @Override
            public HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri)  {
                if( httpMethod.equals(HttpMethod.GET) ) {
                    HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = new HttpEntityEnclosingRequestBase () {
                        @Override
                        public String getMethod() {
                            return HttpMethod.GET.name();
                        }
                    };
                    httpEntityEnclosingRequestBase.setURI(uri);
                    return httpEntityEnclosingRequestBase;
                }else{
                    return super.createHttpUriRequest(httpMethod, uri);
                }
            }
        });

        return rest;
    }
}
