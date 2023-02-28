package test.com.pmrodrigues.users.integrations;

import com.pmrodrigues.users.UserApplication;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.service.UserService;
import lombok.val;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(SpringExtension.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ITUserController {
    @Value("${KEYCLOAK_LOCATION:http://localhost:8080/auth}")
    private String SERVER_URL;
    @Value("${KEYCLOAK_REALM:master}")
    private String REALM;
    private String USERNAME = "admin";
    private String PASSWORD = "admin";
    @Value("${KEYCLOAK_CLIENT_ID:94cf4fee-1b57-4e3c-8d97-195e7f7f1173}")
    private String CLIENT_ID;
    @Value("${KEYCLOAK_CLIENT_SECRET:gNjirmWqaUiP4NWREgRDpbzJpnq7WSZD}")
    private String CLIENT_SECRET;

    private String USER_API_URL = "http://localhost:8143";
    private RestTemplate rest;

    @Autowired
    private UserService userService;



    @BeforeEach
    public void beforeEach() {
        val keycloak = KeycloakBuilder
                .builder()
                .serverUrl(SERVER_URL)
                .realm(REALM)
                .username(USERNAME)
                .password(PASSWORD)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .scope("openid")
                .grantType("client_credentials")
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
        val token = keycloak.tokenManager().getAccessTokenString();

        this.rest = new RestTemplateBuilder().rootUri(USER_API_URL)
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build();
    }

    @Test
    void testAddUser() {
        val user = User.builder()
                .firstName("new_user")
                .lastName("insert")
                .email("to_insert@test.com")
                .build();
        val response = rest.postForLocation("/users", user);
        assertNotNull(response);

        val returned = rest.getForEntity(USER_API_URL + response, User.class);
        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(user.getEmail(), returned.getBody().getEmail());
        assertNotNull(returned.getBody().getExternalId());
    }


    @Test
    void testUpdateUser() {

        val user = User.builder()
                .firstName("to_update")
                .lastName("to_update")
                .email("to_update@test.com")
                .build();

        val response = rest.postForLocation("/users", user);
        assertNotNull(response);

        user.setFirstName("changed_user");
        val entity = new HttpEntity(user);
        val returned = rest.exchange(USER_API_URL + response, HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.OK, returned.getStatusCode());

    }

    @Test
    void testFindUserById() {

        val user = User.builder()
                .firstName("to_get")
                .lastName("to_get")
                .email("to_get@test.com")
                .build();

        val response = rest.postForLocation("/users", user);
        assertNotNull(response);

        val returned = rest.getForEntity(USER_API_URL + response, User.class);
        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(user.getEmail(), returned.getBody().getEmail());
        assertEquals(user.getFirstName(), returned.getBody().getFirstName());
        assertEquals(user.getLastName(), returned.getBody().getLastName());
        assertNotNull(returned.getBody().getExternalId());
    }

    @Test
    void testListUsers() {
        val returned = rest.getForEntity(USER_API_URL + "/users", Page.class);
        assertEquals(HttpStatus.OK, returned.getStatusCode());
    }

    @AfterAll
    @Transactional
    public void afterAll() {

        val emails = List.of("to_insert@test.com", "to_get@test.com", "to_update@test.com");
        emails.stream()
                .map(email -> User.builder().email(email).build())
                .forEach(user -> userService.delete(user));


    }

}
