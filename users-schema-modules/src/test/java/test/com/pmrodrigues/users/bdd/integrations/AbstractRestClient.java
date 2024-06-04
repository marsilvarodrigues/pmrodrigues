package test.com.pmrodrigues.users.bdd.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestTemplate;
import test.com.pmrodrigues.users.helper.HelperPage;

import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.ThreadLocal.withInitial;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@RequiredArgsConstructor
abstract class AbstractRestClient<E> {

    private final String SERVER_URL;
    private final String REALM;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final ObjectMapper mapper = new ObjectMapper();

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private URI location;
    private Class<E> parameterizedType;
    @Getter
    private HttpStatus httpStatus;
    @Getter
    @Setter
    private UUID id;
    @Getter
    @Setter
    private E entity;
    private static final ThreadLocal<Map<String, Object>> context = withInitial(HashMap::new);

    public RestTemplate getRest() {
        val username = (String)context.get().getOrDefault("USERNAME", "admin");
        val password = (String)context.get().getOrDefault("PASSWORD", "admin");
        val token = this.generateToken(username, password);
        return initRestTemplate(token);
    }

    protected String generateToken(@NonNull String username, @NonNull String password) {

        context.get().put("USERNAME", username);
        context.get().put("PASSWORD", password);

        val keycloak = KeycloakBuilder
                .builder()
                .serverUrl(SERVER_URL)
                .realm(REALM)
                .username(username)
                .password(password)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .grantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue())
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();


            val token = keycloak.tokenManager().getAccessTokenString();
            context.get().put("BEARER_TOKEN", token);
            return token;
    }

    protected RestTemplate initRestTemplate(String token) {
        val rest = new RestTemplateBuilder().rootUri(this.getURL())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .interceptors((request, body, execution) -> {
                    log.info("request headers {} and body {}", request.getHeaders(), new String(body));
                    return execution.execute(request, body);
                })
                .build();

        log.debug("auth token {}", token);

        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory() {
            @Override
            public HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
                if (httpMethod.equals(GET)) {
                    HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = new HttpEntityEnclosingRequestBase() {
                        @Override
                        public String getMethod() {
                            return GET.name();
                        }
                    };
                    httpEntityEnclosingRequestBase.setURI(uri);
                    return httpEntityEnclosingRequestBase;
                } else {
                    return super.createHttpUriRequest(httpMethod, uri);
                }
            }
        });

        return rest;
    }

    protected Class<E> getParameterizedType() {
        if (this.parameterizedType == null) {
            this.parameterizedType = (Class<E>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return parameterizedType;
    }

    protected E post(E e) {
        this.location = this.getRest().postForLocation(this.getURL(), e);
        val id = UUID.fromString(StringUtils.substringAfterLast(this.location.getPath() , "/"));
        return get(id);

    }

    protected E get(UUID id) {
        val response =  this.getRest().getForEntity(this.getURL() + "/" + id, this.getParameterizedType());
        this.httpStatus = response.getStatusCode();
        this.entity = response.getBody();
        return this.entity;
    }

    protected E get() {
        val response =  this.getRest().getForEntity(this.getURL() + "/" + id, this.getParameterizedType());
        this.httpStatus = (HttpStatus) response.getStatusCode();
        return response.getBody();
    }

    protected E put() {
        val httpEntity = new HttpEntity<E>(entity);
        val response = this.getRest().exchange(this.getURL() + "/" + id, HttpMethod.PUT, httpEntity, this.getParameterizedType());
        this.httpStatus = (HttpStatus) response.getStatusCode();
        this.entity = response.getBody();
        return this.entity;
    }

    protected List<E> search(HttpEntity<E> httpEntity) {
        val responseType = new ParameterizedTypeReference<HelperPage<E>>() {};
        val response = this.getRest().exchange(this.getURL(), GET, httpEntity, responseType);
        this.httpStatus = response.getStatusCode();

        val type = mapper.getTypeFactory()
                .constructParametricType(HelperPage.class, this.getParameterizedType());

        HelperPage<E> result =  mapper.convertValue(response.getBody(), type);

        return result.getContent();
    }

    protected void deleteEntity() {
        this.delete(this.entity);
        this.entity = null;
    }

    public void delete(E e) {
        UUID id = (UUID) this.getValue(e, "id");
        this.getRest().delete(this.getURL() + "/" + id);
    }

    @SneakyThrows
    public void setValue(String propertyName, String newValue) {
        this.setValue(this.entity, propertyName, newValue);
    }

    @SneakyThrows
    public void setValue(Object o, String propertyName, String newValue){
        val property = o.getClass().getDeclaredField(propertyName);
        property.setAccessible(true);
        property.set(o, newValue);
    }

    @SneakyThrows
    public Object getValue(String propertyName) {
        return this.getValue(this.entity, propertyName);
    }

    @SneakyThrows
    public Object getValue(Object o, String propertyName) {
        val property = o.getClass().getDeclaredField(propertyName);
        property.setAccessible(true);
        return property.get(o);
    }

    public abstract String getURL();
}
