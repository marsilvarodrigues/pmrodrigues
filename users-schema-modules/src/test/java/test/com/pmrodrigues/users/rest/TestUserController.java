package test.com.pmrodrigues.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.controlleradvices.DuplicatedKeyControllerAdvice;
import com.pmrodrigues.commons.controlleradvices.ValidationErrorControllerAdvice;
import com.pmrodrigues.commons.exceptions.KeycloakIntegrationFailed;
import com.pmrodrigues.security.configurations.WebSecurityConfiguration;
import com.pmrodrigues.users.dtos.UserDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static java.lang.String.format;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(excludeAutoConfiguration = { WebSecurityConfiguration.class })
@ContextConfiguration(classes = {
        UserController.class,
        ValidationErrorControllerAdvice.class,
        DuplicatedKeyControllerAdvice.class} )
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
class TestUserController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserService userService;

    @Test
    @SneakyThrows
    @DisplayName("Should get a user by id")
     void shouldGet()  {

        given(userService.findById(any(UUID.class))).willReturn(new User());

        mvc.perform(get(format("/users/%s",UUID.randomUUID()))
                        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 404 if don't found a user")
     void shouldNotGet() {
        given(userService.findById(any(UUID.class))).willThrow(UserNotFoundException.class);

        mvc.perform(get(format("/users/%s",UUID.randomUUID()))
                        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should save a new user")
     void shouldSaveUser() {
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
     void shouldNotSaveUserExistOtherSaved() {

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
     void shouldNotSaveUserNotValid() {

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
     void shouldDeleteUser(){
        given(userService.findById(any(UUID.class))).willReturn(new User());

        mvc.perform(delete("/users/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should not delete user by Id - User Not Found")
     void shouldNotDeleteUserNotFound(){
        given(userService.findById(any(UUID.class))).willThrow(UserNotFoundException.class);

        mvc.perform(delete("/users/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should List all Users")
     void shouldListAllUsers(){
        given(userService.findAll(any(User.class), any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should List all Users")
     void shouldListAllUsersWithBody(){

        given(userService.findAll(any(User.class), any(PageRequest.class))).willReturn(Page.empty());

        var user = UserDTO.builder().firstName("test").build();
        var body = new ObjectMapper().writeValueAsString(user);

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    @DisplayName("Should List a page of all user")
     void shouldGetAPageOfAllUsers(){

        given(userService.findAll(any(User.class),
                                  any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/users?page=1&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Sort sort = Sort.by(Sort.Order.desc("id"));

        verify(userService).findAll(new User(),PageRequest.of(1, 10, sort));

    }

    @Test
    @SneakyThrows
    @DisplayName("Should List All user sorted")
     void shouldGetAllUsersSortedList(){

        given(userService.findAll(any(User.class),any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/users?sort=email.desc&sort=firstName.asc&sort=lastName.desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Sort sort = Sort.by(Sort.Order.desc("email"),
                            Sort.Order.asc("firstName"),
                            Sort.Order.desc("lastName"));

        verify(userService).findAll(any(User.class),eq(PageRequest.of(0, 50, sort)));

    }

    @Test
    @SneakyThrows
    @DisplayName("Should List All user sorted")
     void shouldGetAllUsersSorted(){

        given(userService.findAll(any(User.class),any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/users?sort=email.desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    @DisplayName("Should not sort")
     void shouldNotSort(){

        given(userService.findAll(any(User.class),any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/users?sort=desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sort=desc").hasJsonPath());



    }


    @Test
    @SneakyThrows
     void shouldUpdateUser(){
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
     void shouldNotUpdateUserUserNotFound(){

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
     void shouldNotUpdateKeycloackIntegrationFailed(){

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
