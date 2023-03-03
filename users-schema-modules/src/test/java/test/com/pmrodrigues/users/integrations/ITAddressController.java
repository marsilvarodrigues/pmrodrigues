package test.com.pmrodrigues.users.integrations;

import com.pmrodrigues.users.UserApplication;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.repositories.StateRepository;
import com.pmrodrigues.users.service.UserService;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import static com.pmrodrigues.users.specifications.SpecificationAddress.address;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ITAddressController extends AbstractITController {

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

    @BeforeEach
    public void beforeEach() {

        this.rest = generateToken(this.owner.getEmail(), this.owner.getPassword());

    }

    @Test
    void shouldAddMyNewAddress() {

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
    void shouldAddAddressForOther() {


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
    void shouldntAddAddressForOther() {
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

