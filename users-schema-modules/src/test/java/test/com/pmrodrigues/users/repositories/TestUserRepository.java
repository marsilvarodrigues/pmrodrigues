package test.com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.UserRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.pmrodrigues.users.specifications.SpecificationUser.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.data.jpa.domain.Specification.where;

@DataJpaTest()
@ContextConfiguration(classes = UserRepository.class)
@EnableJpaRepositories(basePackages = {"com.pmrodrigues.users.repositories"})
@EntityScan("com.pmrodrigues.users.model")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TestUserRepository {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSave(){
        val user = userRepository.save(User.builder().firstName("teste")
                        .lastName("teste")
                .email("teste@teste.com")
                .build());

        var saved = entityManager.find(User.class, user.getId());
        assertEquals(user, saved);

    }

    @Test
    void shouldFindByEmail(){

        val user = User.builder().firstName("teste")
                .lastName("teste")
                .email("teste@teste.com")
                        .expiredDate(LocalDateTime.now().plusDays(1))
                        .createdAt(Instant.now())
                .build();
        entityManager.persist(user);

        val found = userRepository.findByEmail("teste@teste.com");
        assertFalse(found.isEmpty());

    }

    @Test
    void shouldFindAllByCriteria() {
        IntStream.range(1, 101).forEach(index -> {
            val user = User.builder()
                    .firstName("firstName_" + index)
                    .lastName("lastName_" + index)
                    .email("firstName_" + index + ".lastName_" + index + "@test.com")
                    .expiredDate(LocalDateTime.now().minus(1, ChronoUnit.MONTHS))
                    .createdAt(Instant.now())
                    .build();
            entityManager.persist(user);
        });

        var found = userRepository.findAll(where(firstName("firstName_10")), PageRequest.of(0, 10));
        assertEquals(2L, found.getTotalElements());

        found = userRepository.findAll(where(firstName("firstName_10")).
                                     and(lastName("lastName_100")),
                PageRequest.of(0, 10));

        assertEquals(1L, found.getTotalElements());

    }

    @Test
    void shouldFindAllByExternalIds() {

        val externalIds = new ArrayList<UUID>();

        IntStream.range(1, 101).forEach(index -> {
            val user = User.builder()
                    .firstName("firstName_" + index)
                    .lastName("lastName_" + index)
                    .email("firstName_" + index + ".lastName_" + index + "@test.com")
                    .externalId(UUID.randomUUID())
                    .expiredDate(LocalDateTime.now().minus(1, ChronoUnit.MONTHS))
                    .createdAt(Instant.now())
                    .build();
            externalIds.add(user.getExternalId());
            entityManager.persist(user);
        });

        var found = userRepository.findAll(where(externalId(externalIds)), PageRequest.of(0, 10));
        assertEquals(100L, found.getTotalElements());



    }
}