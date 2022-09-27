package test.com.pmrodrigues.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.controlleradvices.DuplicatedKeyControllerAdvice;
import com.pmrodrigues.commons.controlleradvices.ValidationErrorControllerAdvice;
import com.pmrodrigues.commons.exceptions.KeycloakIntegrationFailed;
import com.pmrodrigues.security.configurations.WebSecurityConfiguration;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.rest.UserController;
import com.pmrodrigues.users.service.UserService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(excludeAutoConfiguration = { WebSecurityConfiguration.class })
@ContextConfiguration(classes = {
        UserController.class,
        ValidationErrorControllerAdvice.class,
        DuplicatedKeyControllerAdvice.class} )
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
public class TestUserController {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows
    @DisplayName("Should get a user by id")
    public void shouldGet()  {

        given(userService.findById(any(UUID.class))).willReturn(new User());

        mvc.perform(get(format("/users/%s",UUID.randomUUID()))
                        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 404 if don't found a user")
    public void shouldNotGet() {
        given(userService.findById(any(UUID.class))).willThrow(UserNotFoundException.class);

        mvc.perform(get(format("/users/%s",UUID.randomUUID()))
                        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should save a new user")
    public void shouldSaveUser() {
        val user = User.builder().email("test@test.com").firstName("teste")
                .lastName("teste").build();
        given(userService.createNewUser(any(User.class))).willReturn(user);

        val json = objectMapper.writeValueAsString(user);

        mvc.perform(post("/users")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @Test
    @SneakyThrows
    @DisplayName("Should not save a new user. There is other saved with the same email")
    public void shouldNotSaveUserExistOtherSaved() {

        willThrow(DuplicateKeyException.class).given(userService).createNewUser(any(User.class));

        val user = User.builder().email("test@test.com").firstName("teste")
                .lastName("teste").build();

        val json = objectMapper.writeValueAsString(user);

        mvc.perform(post("/users")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isConflict());

    }

    @Test
    @SneakyThrows
    @DisplayName("Should not save a new user. User not valid")
    public void shouldNotSaveUserNotValid() {

        val user = User.builder().build();
        val json = objectMapper.writeValueAsString(user);

        mvc.perform(post("/users")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @SneakyThrows
    @DisplayName("Should delete user by Id")
    public void shouldDeleteUser(){
        given(userService.findById(any(UUID.class))).willReturn(new User());

        mvc.perform(delete("/users/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should not delete user by Id - User Not Found")
    public void shouldNotDeleteUserNotFound(){
        given(userService.findById(any(UUID.class))).willThrow(UserNotFoundException.class);

        mvc.perform(delete("/users/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should List all Users")
    public void shouldListAllUsers(){
        given(userService.findAll()).willReturn(new ArrayList<>());

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void shouldUpdateUser(){
        willDoNothing().given(userService).updateUser(any(UUID.class),any(User.class));

        val user = User.builder()
                .email("test@test.com")
                .firstName("teste")
                .lastName("teste")
                .externalId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .build();

        val json = objectMapper.writeValueAsString(user);

        mvc.perform(put("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void shouldNotUpdateUserUserNotFound(){

        willThrow(UserNotFoundException.class).given(userService).updateUser(any(UUID.class),any(User.class));

        val user = User.builder()
                .email("test@test.com")
                .firstName("teste")
                .lastName("teste")
                .externalId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .build();

        val json = objectMapper.writeValueAsString(user);

        mvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void shouldNotUpdateKeycloackIntegrationFailed(){

        willThrow(KeycloakIntegrationFailed.class).given(userService).updateUser(any(UUID.class),any(User.class));

        val user = User.builder()
                .email("test@test.com")
                .firstName("teste")
                .lastName("teste")
                .externalId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .build();

        val json = objectMapper.writeValueAsString(user);

        mvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}
