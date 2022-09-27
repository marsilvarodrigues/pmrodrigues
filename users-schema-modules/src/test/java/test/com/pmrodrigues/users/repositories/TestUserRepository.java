package test.com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.UserRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    public void shouldSave(){
        val user = userRepository.save(User.builder().firstName("teste")
                        .lastName("teste")
                .email("teste@teste.com")
                .build());

        var saved = entityManager.find(User.class, user.getId());
        assertEquals(user, saved);

    }

    @Test
    public void shouldFindByEmail(){

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
}