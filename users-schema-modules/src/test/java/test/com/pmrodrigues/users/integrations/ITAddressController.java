package test.com.pmrodrigues.users.integrations;

import com.pmrodrigues.users.UserApplication;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.repositories.StateRepository;
import com.pmrodrigues.users.service.UserService;
import lombok.NonNull;
import lombok.val;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.pmrodrigues.users.specifications.SpecificationAddress.address;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ITAddressController {

    @Value("${KEYCLOAK_LOCATION:http://localhost:8080/auth}")
    private String SERVER_URL;
    @Value("${KEYCLOAK_REALM:master}")
    private String REALM;
    @Value("${KEYCLOAK_CLIENT_ID:94cf4fee-1b57-4e3c-8d97-195e7f7f1173}")
    private String CLIENT_ID;
    @Value("${KEYCLOAK_CLIENT_SECRET:gNjirmWqaUiP4NWREgRDpbzJpnq7WSZD}")
    private String CLIENT_SECRET;

    private String API_URL = "http://localhost:8143";
    private RestTemplate rest;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserService userService;
    private User owner;

    @BeforeAll
    @Transactional
    public void beforeAll() {
        this.owner = User.builder()
                .email("owner@test.com")
                .firstName("OWNER")
                .lastName("OWNER")
                .build();

        val users = userService.findAll(this.owner, PageRequest.of(0,1));
        if( users.getTotalElements() == 1)
            this.owner = users.stream().findFirst().get();
        else
            userService.createNewUser(owner);
    }

    public RestTemplate generateToken(@NonNull String username, @NonNull String password) {
        val keycloak = KeycloakBuilder
                .builder()
                .serverUrl(SERVER_URL)
                .realm(REALM)
                .username(this.owner.getEmail())
                .password(this.owner.getPassword())
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .grantType(AuthorizationGrantType.PASSWORD.getValue())
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();


        val token = keycloak.tokenManager().getAccessTokenString();
        return  new RestTemplateBuilder().rootUri(API_URL)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    @BeforeEach
    public void beforeEach() {

        this.rest = generateToken(this.owner.getEmail(), this.owner.getPassword());

    }

    @Test
    public void shouldAddMyNewAddress() {

        val address = Address.builder()
                        .address1("TESTE")
                        .addressType(AddressType.STREET)
                        .city("TEST")
                        .zipcode("12345-123")
                        .state(stateRepository.findByCode("AC").get())
                        .neightboor("TEST")
                        .build();

        val response = this.rest.postForLocation(API_URL + "/addresses", address);
        assertNotNull(response);

        val returned = this.rest.getForEntity(API_URL + response, Address.class);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(address.getAddress1(), returned.getBody().getAddress1());
        assertEquals(this.owner, returned.getBody().getOwner());
        assertNotNull(returned.getBody().getId());

    }

    @Test
    public void shouldAddAddressForOther() {


        val address = Address.builder()
                .address1("TESTE")
                .addressType(AddressType.STREET)
                .city("TEST")
                .zipcode("12345-123")
                .state(stateRepository.findByCode("AC").get())
                .neightboor("TEST")
                .owner(owner)
                .build();

        var rest = generateToken("admin", "admin");

        val response = rest.postForLocation(API_URL + "/addresses", address);
        assertNotNull(response);

        val returned = rest.getForEntity(API_URL + response, Address.class);

        assertEquals(HttpStatus.OK, returned.getStatusCode());
        assertEquals(address.getAddress1(), returned.getBody().getAddress1());
        assertEquals(this.owner, returned.getBody().getOwner());
        assertNotNull(returned.getBody().getId());

    }

    @Test
    public void shouldntAddAddressForOther() {
        var owner = User.builder()
                .email("owner2@test.com")
                .firstName("OWNER")
                .lastName("OWNER")
                .build();

        userService.createNewUser(owner);

        val address = Address.builder()
                .address1("TESTE")
                .addressType(AddressType.STREET)
                .city("TEST")
                .zipcode("12345-123")
                .state(stateRepository.findByCode("AC").get())
                .neightboor("TEST")
                .owner(owner)
                .build();

        var exception = assertThrows(HttpClientErrorException.class, () -> rest.postForLocation(API_URL + "/addresses", address));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());


    }


    @AfterAll
    @Transactional
    public void afterAll() {

        userService.delete(this.owner);

    }

    @AfterEach
    @Transactional
    public void afterEach() {

        userService.findAll(User.builder().email("owner2@test.com").build(),
                PageRequest.of(0,1))
                .stream()
                .findFirst()
                .ifPresent(u -> userService.delete(u));

        var toDelete = addressRepository.findAll(Specification.where(address("TEST")));
        addressRepository.deleteAll(toDelete);

    }

}

