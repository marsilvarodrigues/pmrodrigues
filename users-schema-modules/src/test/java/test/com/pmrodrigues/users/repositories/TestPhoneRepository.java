package test.com.pmrodrigues.users.repositories;

import com.pmrodrigues.security.audit.SpringSecurityAuditAwareImpl;
import com.pmrodrigues.users.model.Phone;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.PhoneType;
import com.pmrodrigues.users.repositories.PhoneRepository;
import com.pmrodrigues.users.repositories.UserRepository;
import lombok.val;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.pmrodrigues.users.specifications.SpecificationPhone.type;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest()
@ContextConfiguration(classes = {PhoneRepository.class, UserRepository.class, SpringSecurityAuditAwareImpl.class})
@EnableJpaRepositories(basePackages = {"com.pmrodrigues.users.repositories"})
@EnableJpaAuditing
@EntityScan("com.pmrodrigues.users.model")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestPhoneRepository {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PhoneRepository phoneRepository;

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
    void shoudAddAnewPhone() {

        val phone = Phone.builder().phoneNumber("1234")
                .type(PhoneType.CELLPHONE)
                .owner(user).build();

        val saved = phoneRepository.save(phone);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedBy());
        assertNotNull(saved.getUpdatedBy());

    }

    @Test
    void shouldList() {

        HashMap<PhoneType, Long> counts = new HashMap<>();
        Random random = new Random();
        val phoneTypes = PhoneType.values();
        for( PhoneType type : phoneTypes) {
            counts.put(type, 0L);
        }

        IntStream.range(1, 11).forEach( i -> {

            int randomIndex = random.nextInt(phoneTypes.length);
            PhoneType randomPhoneType = phoneTypes[randomIndex];
            counts.put(randomPhoneType, counts.get(randomPhoneType) + 1);

            val phone = Phone.builder().phoneNumber(RandomStringUtils.randomNumeric(10))
                    .type(randomPhoneType)
                    .owner(user).build();
            phoneRepository.save(phone);
        });

        val phones = phoneRepository.findAll(type(PhoneType.CELLPHONE), PageRequest.of(0, 10));
        assertFalse(phones.isEmpty());
        assertEquals(counts.get(PhoneType.CELLPHONE), phones.getTotalElements());
    }
}
