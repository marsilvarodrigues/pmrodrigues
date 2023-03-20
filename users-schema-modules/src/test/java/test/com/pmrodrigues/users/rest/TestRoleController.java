package test.com.pmrodrigues.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.controlleradvices.DuplicatedKeyControllerAdvice;
import com.pmrodrigues.commons.controlleradvices.ValidationErrorControllerAdvice;
import com.pmrodrigues.security.configurations.WebSecurityConfiguration;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.users.exceptions.RoleNotFoundException;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.rest.RoleController;
import com.pmrodrigues.users.service.RoleService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(excludeAutoConfiguration = {WebSecurityConfiguration.class})
@ContextConfiguration(classes = {
        RoleController.class,
        ValidationErrorControllerAdvice.class,
        DuplicatedKeyControllerAdvice.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
public class TestRoleController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mvc;
    @MockBean
    private RoleService roleService;


    @Test
    @SneakyThrows
    void shouldApplyRoleToUser() {

        willDoNothing().given(roleService).applyRoleToUser(any(User.class), anyString());

        mvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON)
                .param("role", Security.SYSTEM_ADMIN)
                .param("user", UUID.randomUUID().toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @Test
    @SneakyThrows
    void shoudNotApplyRoleNotFound() {
        willThrow(RoleNotFoundException.class).given(roleService).applyRoleToUser(any(User.class), anyString());

        mvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON)
                        .param("role", Security.SYSTEM_ADMIN)
                        .param("user", UUID.randomUUID().toString()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shoudNotApplyUserNotFound() {
        willThrow(UserNotFoundException.class).given(roleService).applyRoleToUser(any(User.class), anyString());

        mvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON)
                        .param("role", Security.SYSTEM_ADMIN)
                        .param("user", UUID.randomUUID().toString()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldListRoleByRoleName() {
        given(roleService.getUserInRole(any(String.class),any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/roles?role=XXX"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldListRoleByRoleNamePageable() {
        given(roleService.getUserInRole(any(String.class),any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/roles?role=XXX&page=1&size=10"))
                .andDo(print())
                .andExpect(status().isOk());

        Sort sort = Sort.by(Sort.Order.desc("id"));

        verify(roleService).getUserInRole("XXX", PageRequest.of(1, 10, sort));
    }
}
