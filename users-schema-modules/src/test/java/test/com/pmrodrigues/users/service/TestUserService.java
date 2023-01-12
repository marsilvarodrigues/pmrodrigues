package test.com.pmrodrigues.users.service;


import com.pmrodrigues.commons.dtos.Email;
import com.pmrodrigues.commons.exceptions.KeycloakIntegrationFailed;
import com.pmrodrigues.users.clients.EmailClient;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.KeycloakUserRepository;
import com.pmrodrigues.users.repositories.UserRepository;
import com.pmrodrigues.users.service.UserService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.KeycloakPrincipal;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TestUserService {

    @InjectMocks
    private UserService userService;

    @Mock
    private EmailClient emailService;

    @Mock
    private KeycloakUserRepository userClient;

    @Mock
    private UserRepository repository;

    @Test
    @DisplayName("Should save a new user")
    @SneakyThrows
    void shouldSave() {

        val user = User.builder().id(UUID.randomUUID()).firstName("teste")
                .lastName("teste").email("test@test.com").build();
        Optional<User> optional = Optional.empty();

        given(repository.findByEmail(any(String.class))).willReturn(optional);
        given(userClient.getUserIdByEmail(any(String.class))).willReturn(Map.of());
        given(repository.save(any(User.class))).willReturn(user);
        given(userClient.insert(any(User.class))).willReturn(UUID.randomUUID());
        given(emailService.getEmailByName(any(String.class))).willReturn(ResponseEntity.ok(new Email()));
        given(emailService.send(any(Email.class))).willReturn(ResponseEntity.ok().build());

        userService.createNewUser(
                User.builder()
                .email("teste@teste.com")
                .build());

        verify(emailService).send(any(Email.class));

    }

    @Test
    @DisplayName("Should not save a new user with existed other with the same email")
     void shouldNotSave() {
        val user = User.builder().email("").build();
        Optional<User> optional = Optional.of(new User());

        given(repository.findByEmail(any(String.class))).willReturn(optional);

        assertThrows(DuplicateKeyException.class, () -> userService.createNewUser(user));
    }

    @Test
    @DisplayName("Should not save a new user with existed other with the same email")
    void shouldNotSaveExistedOnKeyCloak() {
        val user = User.builder().email("").build();
        Optional<User> optional = Optional.empty();

        given(repository.findByEmail(any(String.class))).willReturn(optional);
        given(userClient.getUserIdByEmail(any(String.class))).willReturn(Map.of("",""));

        assertThrows(DuplicateKeyException.class, () -> userService.createNewUser(user));
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        val user = User.builder().id(UUID.randomUUID()).build();
        given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
        willDoNothing().given(repository).delete(any(User.class));

        willDoNothing().given(userClient).delete(any(UUID.class));

        userService.delete(user);
    }

    @Test
    @DisplayName("Should not delete user because he doesn't exist")
    void shouldNotDeleteUserNotFound(){
        val user = User.builder().id(UUID.randomUUID()).build();
        given(repository.findById(any(UUID.class))).willReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () ->{
            userService.delete(user);
        });

    }

    @Test
    @DisplayName("Should not delete user because integration failed")
    void shouldNotDeleteIntegrationFailed(){
        val user = User.builder().id(UUID.randomUUID()).build();
        given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
        willDoNothing().given(repository).delete(any(User.class));

        willThrow(new KeycloakIntegrationFailed()).given(userClient).delete(any(UUID.class));

        assertThrows(KeycloakIntegrationFailed.class, () ->{
            userService.delete(user);
        });

    }

    @Test
    void shouldUpdateUser(){

        val id = UUID.randomUUID();
        val user = User.builder()
                .email("teste")
                .firstName("teste")
                .lastName("teste")
                .externalId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .build();

        given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
        given(repository.save(any(User.class))).willReturn(user);
        willDoNothing().given(userClient).update(user);

        userService.updateUser(id, user);
    }

    @Test
    void shouldNotUpdateUserNotFound() {

        val id = UUID.randomUUID();

        val user = User.builder()
                .email("teste")
                .firstName("teste")
                .lastName("teste")
                .externalId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .build();

        given(repository.findById(any(UUID.class))).willReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> userService.updateUser(id, user));
    }

    @Test
    void shouldNotUpdateUserKeycloackIntegrationFail(){

        val id = UUID.randomUUID();

        val user = User.builder()
                .email("teste")
                .firstName("teste")
                .lastName("teste")
                .externalId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .build();

        given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
        given(repository.save(any(User.class))).willReturn(user);
        willThrow(new KeycloakIntegrationFailed()).given(userClient).update(any(User.class));

        assertThrows(KeycloakIntegrationFailed.class, () -> userService.updateUser(id, user));
    }


    @Test
    void shouldGetLoggedUser() {

        val user = new User();

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(new KeycloakPrincipal(UUID.randomUUID().toString(), null)
                                , UUID.randomUUID(), Collections.emptyList())
                );

        given(repository.findByExternalId(any(UUID.class))).willReturn(Optional.of(user));

        assertNotNull(userService.getAuthenticatedUser());

    }

}