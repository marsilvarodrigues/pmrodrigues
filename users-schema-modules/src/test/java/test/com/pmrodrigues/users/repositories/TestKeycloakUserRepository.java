package test.com.pmrodrigues.users.repositories;

import com.pmrodrigues.commons.exceptions.KeycloakIntegrationFailed;
import com.pmrodrigues.users.clients.UserClient;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.KeycloakUserRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestKeycloakUserRepository {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private KeycloakUserRepository repository;

    @Test
    void shouldCreateUser() {
        given(userClient.add(any(UserRepresentation.class)))
                .willReturn(ResponseEntity.created(URI.create("http://localhost:8080/auth/admin/realms/master/users/" + UUID.randomUUID())).build());

        val uuid = repository.insert(User.builder().email("teste").firstName("teste").lastName("teste").build());
        assertNotNull(uuid);
    }

    @Test
    void shouldNotCreateUser() {
        given(userClient.add(any(UserRepresentation.class))).willReturn(ResponseEntity.badRequest().build());

        assertThrows(KeycloakIntegrationFailed.class, () ->
            repository.insert(User.builder().email("teste").firstName("teste").lastName("teste").build())
        );
    }

    @Test
    void shouldUpdate() {
        given(userClient.update(any(UUID.class),any(UserRepresentation.class))).willReturn(ResponseEntity.ok().build());
        val user = User.builder()
                            .email("teste")
                            .firstName("teste")
                            .lastName("teste")
                            .externalId(UUID.randomUUID())
                            .build();

        repository.update(user);
    }

    @Test
    void shouldNotUpdate() {
        given(userClient.update(any(UUID.class),any(UserRepresentation.class))).willReturn(ResponseEntity.badRequest().build());
        val user = User.builder()
                .email("teste")
                .firstName("teste")
                .lastName("teste")
                .externalId(UUID.randomUUID())
                .build();

        assertThrows(KeycloakIntegrationFailed.class, () -> repository.update(user));
    }

    @Test
    void shouldGetUserByEmail() {
        final List<UserRepresentation> users = new ArrayList<>();
        IntStream.range(0,1).forEach(index -> {
            val user = new UserRepresentation();
            user.setId(UUID.randomUUID().toString());
            user.setEmail(MessageFormat.format("test_{0}@test.com",index));
            users.add(user);
        });
        given(userClient.getByEmail(any(String.class))).willReturn(ResponseEntity.of(Optional.of(users)));

        val response = repository.getUserIdByEmail("test_0@test.com");
        assertFalse(response.isEmpty());
        assertTrue(response.containsKey("test_0@test.com"));
    }

    @Test
    void shouldNotFound() {
        given(userClient.getByEmail(any(String.class))).willReturn(ResponseEntity.of(Optional.empty()));

        var response = repository.getUserIdByEmail("test_0@test.com");
        assertTrue(response.isEmpty());
    }

    @Test
    void shouldDelete() {
        given(userClient.delete(any(UUID.class))).willReturn(ResponseEntity.ok().build());
        repository.delete(UUID.randomUUID());
    }

    @Test
    void shouldNotDelete() {
        given(userClient.delete(any(UUID.class))).willReturn(ResponseEntity.badRequest().build());
        assertThrows(KeycloakIntegrationFailed.class, () -> repository.delete(UUID.randomUUID()));
    }

}