package test.com.pmrodrigues.users.bdd.stepdefs;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestTemplate;
import test.com.pmrodrigues.users.helper.HelperPage;

import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.ThreadLocal.withInitial;
import static test.com.pmrodrigues.users.bdd.ContextAttribute.*;

@Slf4j
public abstract class AbstractStepsConfiguration<E> {
    protected String API_URL = "http://localhost:8143";
    @Value("${KEYCLOAK_LOCATION:http://localhost:8080/auth}")
    private String SERVER_URL;
    @Value("${KEYCLOAK_REALM:master}")
    private String REALM;
    @Value("${KEYCLOAK_CLIENT_ID:94cf4fee-1b57-4e3c-8d97-195e7f7f1173}")
    private String CLIENT_ID;
    @Value("${KEYCLOAK_CLIENT_SECRET:3FrxqjAubBRrKhn27cemzho7B4x3MIrN}")
    private String CLIENT_SECRET;

    private static final ThreadLocal<Map<String, Object>> context = withInitial(HashMap::new);

    private RestTemplate rest;

    private ResponseEntity returned;

    private Class<E> parameterizedType;

    protected RestTemplate generateToken(@NonNull String username, @NonNull String password) {
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
        initRestTemplate(token);


        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory() {
            @Override
            public HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
                if (httpMethod.equals(HttpMethod.GET)) {
                    HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = new HttpEntityEnclosingRequestBase() {
                        @Override
                        public String getMethod() {
                            return HttpMethod.GET.name();
                        }
                    };
                    httpEntityEnclosingRequestBase.setURI(uri);
                    return httpEntityEnclosingRequestBase;
                } else {
                    return super.createHttpUriRequest(httpMethod, uri);
                }
            }
        });

        put(REST_TEMPLATE, rest);
        return rest;
    }

    private void initRestTemplate(String token) {
        this.rest = new RestTemplateBuilder().rootUri(API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .interceptors((request, body, execution) -> {
                    log.info("request headers {} and body {}", request.getHeaders(), new String(body));
                    return execution.execute(request, body);
                })
                .build();
    }

    protected void put(String attribute, Object value) {
        context.get().put(attribute, value);
    }

    protected Object get(String attribute) {
        return context.get().get(attribute);
    }


    @SneakyThrows
    protected void setValue(String propertyName, String newValue, Object o) {
        val property = o.getClass().getDeclaredField(propertyName);
        property.setAccessible(true);
        property.set(o, newValue);
    }

    @SneakyThrows
    protected Object getValue(String propertyName, Object o) {
        val property = o.getClass().getDeclaredField(propertyName);
        property.setAccessible(true);
        return property.get(o);

    }

    protected ResponseEntity<E> postForLocation(String url, E e, String id, String entity) {
        put(RESPONSE_URL, getRest().postForLocation(url, e));
        return getForEntity(API_URL + get(RESPONSE_URL), id, entity);
    }

    protected RestTemplate getRest() {
        if( this.rest == null ) this.rest = (RestTemplate) get(REST_TEMPLATE);
        return rest;
    }

    protected ResponseEntity<E> getForEntity(String url, String id, String entity) {
        this.returned = getRest().getForEntity(url, this.getParameterizedType());
        put(entity, returned.getBody());
        put(id, getValue("id", returned.getBody()));
        return returned;
    }

    protected void updateEntity(String url, E e) {
        saveTo(e, url, HttpMethod.PUT);
    }

    protected void insertEntity(String url , E e) {
        saveTo(e, url, HttpMethod.POST);
    }

    private void saveTo(E e, String url, HttpMethod post) {
        val httpEntity = new HttpEntity<E>(e);
        this.returned = getRest().exchange(API_URL + url, post, httpEntity, e.getClass());
    }

    protected void searchBySample(String api, HttpMethod httpMethod, HttpEntity httpEntity, ParameterizedTypeReference typeReference) {
        this.returned = getRest().exchange(API_URL + api, httpMethod, httpEntity, typeReference);


    }

    protected Class<E> getParameterizedType() {
        if (parameterizedType == null) {
            parameterizedType = (Class<E>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return parameterizedType;
    }

    protected E getEntity() {
        if (this.returned != null) return (E) this.returned.getBody();
        return null;
    }

    protected void delete(String url) {
        getRest().delete(url);
    }

    protected List<E> listEntity() {
        return ((HelperPage<E>) this.returned.getBody()).getContent();
    }

    protected HttpStatus getStatusCode() {
        if (this.returned != null) return this.returned.getStatusCode();
        return null;
    }

    protected void setReturn(ResponseEntity returned) {
        this.returned = returned;
    }
}
