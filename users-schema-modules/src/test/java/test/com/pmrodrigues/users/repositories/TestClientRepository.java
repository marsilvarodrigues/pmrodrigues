package test.com.pmrodrigues.users.repositories;

import com.pmrodrigues.security.audit.SpringSecurityAuditAwareImpl;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.Client;
import com.pmrodrigues.users.model.Phone;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.model.enums.PhoneType;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.repositories.ClientRepository;
import com.pmrodrigues.users.repositories.StateRepository;
import com.pmrodrigues.users.repositories.UserRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

@DataJpaTest()
@ContextConfiguration(classes = { ClientRepository.class, AddressRepository.class, StateRepository.class, UserRepository.class, SpringSecurityAuditAwareImpl.class})
@EnableJpaRepositories(basePackages = {"com.pmrodrigues.users.repositories"})
@EnableJpaAuditing
@EntityScan("com.pmrodrigues.users.model")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestClientRepository {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    private User user;

    @BeforeEach
    void beforeEach() {
        this.user = User.builder().firstName("teste")
                .lastName("teste")
                .email("teste@teste.com")
                .expiredDate(LocalDateTime.now().plusDays(1))
                .createdAt(Instant.now())
                .externalId(UUID.randomUUID())
                .build();

        userRepository.save(user);


        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(new KeycloakPrincipal(user.getExternalId().toString(), null)
                                , user.getExternalId(), Collections.emptyList())
                );
    }

    @Test
    @Rollback
    void shouldCreateAClient() {

        var client = Client.builder()
                        .birthday(LocalDate.now())
                        .firstName("teste")
                        .lastName("teste")
                        .email("client@test.com")
                .build();
        client = client.add(
                Phone.builder()
                     .phoneNumber("11234-1234")
                     .type(PhoneType.CELLPHONE)
                      .build())
                .add(Address.builder()
                        .addressType(AddressType.ROAD)
                        .state(stateRepository.findByCode("RJ").get())
                        .city("teste")
                        .neighbor("teste")
                        .address1("test")
                        .zipcode("12345-123")
                        .build());

        val saved = clientRepository.save(client);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedBy());
        assertNotNull(saved.getUpdatedBy());

        assertFalse(saved.getPhones().isEmpty());
        assertFalse(saved.getAddresses().isEmpty());


        assertTrue(saved.getPhones().stream().allMatch( p -> p.getId() != null ));
        assertTrue(saved.getPhones().stream().allMatch( p -> p.getOwner() != null ));
        assertTrue(saved.getAddresses().stream().allMatch( p -> p.getId() != null ));
        assertTrue(saved.getAddresses().stream().allMatch( p -> p.getOwner() != null ));

    }

    @Test
    void shouldUpdateAPhoneFromAClient() {

        var client = Client.builder()
                .birthday(LocalDate.now())
                .firstName("teste")
                .lastName("teste")
                .email("client@test.com")
                .build();
        client = client.add(
                        Phone.builder()
                                .phoneNumber("11234-1234")
                                .type(PhoneType.CELLPHONE)
                                .build())
                .add(Address.builder()
                        .addressType(AddressType.ROAD)
                        .state(stateRepository.findByCode("RJ").get())
                        .city("teste")
                        .neighbor("teste")
                        .address1("test")
                        .zipcode("12345-123")
                        .build());

        val saved = clientRepository.save(client);

        saved.add(Phone.builder().phoneNumber("321").type(PhoneType.HOME).build());
        clientRepository.save(saved);

        assertEquals(2, saved.getPhones().size());
        assertTrue( saved.getPhones().stream().filter(p -> p.getType() == PhoneType.HOME).allMatch( p-> p.getId() != null ));

        saved.remove(Phone.builder().phoneNumber("321").type(PhoneType.HOME).build());
        clientRepository.save(saved);

        val other = clientRepository.findById(saved.getId()).get();

        assertEquals(1, other.getPhones().size());

    }
}
