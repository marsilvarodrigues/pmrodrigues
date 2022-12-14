package test.com.pmrodrigues.users.repositories;

import com.pmrodrigues.security.audit.SpringSecurityAuditAwareImpl;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.repositories.StateRepository;
import com.pmrodrigues.users.repositories.UserRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest()
@ContextConfiguration(classes = { AddressRepository.class, StateRepository.class, UserRepository.class, SpringSecurityAuditAwareImpl.class})
@EnableJpaRepositories(basePackages = {"com.pmrodrigues.users.repositories"})
@EnableJpaAuditing
@EntityScan("com.pmrodrigues.users.model")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TestAddressRepository {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;


    @BeforeEach
    public void beforeEach() {
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
                        new UsernamePasswordAuthenticationToken(user.getId(), user.getExternalId(), Collections.emptyList())
                );


    }


    @Test
    public void shouldSaveAddress() {

        val state = stateRepository.findByCode("RJ").get();
        val address = Address.builder()
                .state(state)
                .owner(user)
                .address1("TESTE")
                .neightboor("TESTE")
                .city("TESTE")
                .zipcode("TESTE")
                .addressType(AddressType.STREET)
                .build();

        val saved = addressRepository.save(address);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedBy());
        assertNotNull(saved.getUpdatedBy());

    }

    @Test
    public void shouldListMyAddress() {
        val state = stateRepository.findByCode("RJ").get();
        val address = Address.builder()
                .state(state)
                .owner(user)
                .address1("TESTE")
                .neightboor("TESTE")
                .city("TESTE")
                .zipcode("TESTE")
                .addressType(AddressType.STREET)
                .build();

        val saved = addressRepository.save(address);


        val addresses = addressRepository.findByOwner(user);
        assertTrue(addresses.contains(saved));
    }

}