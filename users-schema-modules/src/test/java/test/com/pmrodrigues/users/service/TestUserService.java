package test.com.pmrodrigues.users.service;


import com.pmrodrigues.commons.dtos.Email;
import com.pmrodrigues.commons.exceptions.KeycloakIntegrationFailed;
import com.pmrodrigues.commons.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.clients.EmailClient;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.KeycloakUserRepository;
import com.pmrodrigues.users.repositories.UserRepository;
import com.pmrodrigues.users.service.UserService;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.KeycloakPrincipal;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
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

        userService.create(new UserDTO(null, "test", "test", "test@test.com"));

        verify(emailService).send(any(Email.class));

    }

    @Test
    @DisplayName("Should not save a new user with existed other with the same email")
     void shouldNotSave() {
        val user = User.builder().email("").build();
        Optional<User> optional = Optional.of(new User());

        given(repository.findByEmail(any(String.class))).willReturn(optional);

        assertThrows(DuplicateKeyException.class, () ->
                userService.create(new UserDTO(null, "test", "test", "test@test.com"))
        );
    }

    @Test
    @DisplayName("Should not save a new user with existed other with the same email")
    void shouldNotSaveExistedOnKeyCloak() {

        Optional<User> optional = Optional.empty();

        given(repository.findByEmail(any(String.class))).willReturn(optional);
        given(userClient.getUserIdByEmail(any(String.class))).willReturn(Map.of("",""));

        assertThrows(DuplicateKeyException.class, () -> userService.create(new UserDTO(null, "test", "test", "test@test.com")));
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        val user = User.builder().email("").externalId(UUID.randomUUID()).build();
        given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
        userService.delete(UUID.randomUUID());

        verify(repository).delete(any(User.class));
        verify(userClient).delete(any(UUID.class));
    }

    @Test
    @DisplayName("Should not delete user because he doesn't exist")
    void shouldNotDeleteUserNotFound(){

        given(repository.findById(any(UUID.class))).willReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () ->{
            userService.delete(UUID.randomUUID());
        });

    }

    @Test
    @DisplayName("Should not delete user because integration failed")
    void shouldNotDeleteIntegrationFailed(){
        val user = User.builder().id(UUID.randomUUID()).externalId(UUID.randomUUID()).build();
        given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
        willThrow(new KeycloakIntegrationFailed()).given(userClient).delete(any(UUID.class));

        assertThrows(KeycloakIntegrationFailed.class, () ->{
            userService.delete(UUID.randomUUID());
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

        userService.update(id, new UserDTO(null, "test", "test", "test@test.com"));

        verify(repository).save(any(User.class));
        verify(userClient).update(any(User.class));
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


        assertThrows(UserNotFoundException.class, () -> userService.update(id, new UserDTO(null, "test", "test", "test@test.com")));
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

        assertThrows(KeycloakIntegrationFailed.class, () -> userService.update(id, new UserDTO(null, "test", "test", "test@test.com")));
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
    @Test
    void shouldChangePassword() {

        val mockStatic = mockStatic(SecurityUtils.class);
        val id = UUID.randomUUID();
        try {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            mockStatic.when(() -> SecurityUtils.getUserLoggedId()).thenReturn(Optional.of(id));

            val user = mock(User.class);

            given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
            given(user.getId()).willReturn(id);

            userService.changePassword(id, RandomStringUtils.random(10));

            verify(user, times(1)).setPassword(anyString());
            verify(userClient, times(1)).changePassword(any(User.class));
        }finally{
            mockStatic.close();
        }

    }

    @Test
    void shouldChangePasswordUserIsSystemAdmin() {

        val mockStatic = mockStatic(SecurityUtils.class);
        val id = UUID.randomUUID();
        try {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);
            mockStatic.when(() -> SecurityUtils.getUserLoggedId()).thenReturn(Optional.of(UUID.randomUUID()));

            val user = mock(User.class);

            given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
            given(user.getId()).willReturn(id);

            userService.changePassword(id, RandomStringUtils.random(10));

            verify(user, times(1)).setPassword(anyString());
            verify(userClient, times(1)).changePassword(any(User.class));
        }finally{
            mockStatic.close();
        }

    }

    @Test
    void dontShouldChangePasswordUserNotFound() {

        val id = UUID.randomUUID();
        given(repository.findById(any(UUID.class))).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.changePassword(id, RandomStringUtils.random(10)));

    }

    @Test
    void dontShouldChangePasswordUserIsDifferentThanUserConnected() {
        val mockStatic = mockStatic(SecurityUtils.class);
        val id = UUID.randomUUID();

        try {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            mockStatic.when(() -> SecurityUtils.getUserLoggedId()).thenReturn(Optional.of(UUID.randomUUID()));

            val user = mock(User.class);

            given(repository.findById(any(UUID.class))).willReturn(Optional.of(user));
            given(user.getId()).willReturn(id);

            assertThrows(OperationNotAllowedException.class, () -> userService.changePassword(id, RandomStringUtils.random(10)));

        }finally{
            mockStatic.close();
        }
    }

}