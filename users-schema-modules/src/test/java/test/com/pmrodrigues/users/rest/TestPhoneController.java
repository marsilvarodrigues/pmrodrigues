package test.com.pmrodrigues.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.controlleradvices.DuplicatedKeyControllerAdvice;
import com.pmrodrigues.commons.controlleradvices.ValidationErrorControllerAdvice;
import com.pmrodrigues.commons.exceptions.NotFoundException;
import com.pmrodrigues.security.configurations.WebSecurityConfiguration;
import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.users.dtos.PhoneDTO;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.exceptions.PhoneNotFoundException;
import com.pmrodrigues.users.model.Phone;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.PhoneType;
import com.pmrodrigues.users.rest.PhoneController;
import com.pmrodrigues.users.service.PhoneService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
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

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(excludeAutoConfiguration = { WebSecurityConfiguration.class })
@ContextConfiguration(classes = {
        PhoneController.class,
        ValidationErrorControllerAdvice.class,
        DuplicatedKeyControllerAdvice.class} )
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
class TestPhoneController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PhoneService service;


    @Test
    @SneakyThrows
    void shouldGetPhoneById(){

        given(service.findById(any(UUID.class))).willReturn(Phone.builder()
                .owner(User.builder().build())
                .type(PhoneType.CELLPHONE)
                .phoneNumber("")
                .build());

        mvc.perform(get(format("/phones/%s",UUID.randomUUID()))
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldntGetPhoneById() {
        given(service.findById(any(UUID.class))).willThrow(new NotFoundException());

        mvc.perform(get(format("/phones/%s",UUID.randomUUID()))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldAddANewPhone() {


        val phone = new PhoneDTO(UUID.randomUUID(), new UserDTO(UUID.randomUUID(),null, null, null),null,PhoneType.CELLPHONE);

        val json = objectMapper.writeValueAsString(phone);
        val returned = Phone.builder()
                    .id(UUID.randomUUID())
                    .type(PhoneType.CELLPHONE)
                    .phoneNumber("")
                    .owner(User.builder().id(phone.owner().id()).build())
                    .build();

        given(service.createNewPhone(any(PhoneDTO.class))).willReturn(returned);



        mvc.perform(post("/phones")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/phones/" + returned.getId().toString()));
    }

    @Test
    @SneakyThrows
    void shouldntAddPhoneUserNotAllowed() {

        val phone = new PhoneDTO(UUID.randomUUID(), new UserDTO(UUID.randomUUID(),null, null, null),null,PhoneType.CELLPHONE);

        val json = objectMapper.writeValueAsString(phone);

        given(service.createNewPhone(any(PhoneDTO.class))).willThrow(new OperationNotAllowedException("User not allowed for this operation"));

        mvc.perform(post("/phones")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void shouldUpdatePhone(){

        val phone = new PhoneDTO(UUID.randomUUID(), new UserDTO(UUID.randomUUID(),null, null, null),null,PhoneType.CELLPHONE);

        willDoNothing().given(service).updatePhone(any(UUID.class), any(PhoneDTO.class));

        val json = objectMapper.writeValueAsString(phone);

        mvc.perform(put("/phones/" + phone.id())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldntUpdatePhoneNotFound(){

        val phone = new PhoneDTO(UUID.randomUUID(), new UserDTO(UUID.randomUUID(),null, null, null),null,PhoneType.CELLPHONE);

        willThrow(new PhoneNotFoundException()).given(service).updatePhone(any(UUID.class), any(PhoneDTO.class));

        val json = objectMapper.writeValueAsString(phone);

        mvc.perform(put("/phones/" + phone.id().toString())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void shouldntUpdatPhoneOperationNotAllowed(){

        val phone = new PhoneDTO(UUID.randomUUID(), new UserDTO(UUID.randomUUID(),null, null, null),null,PhoneType.CELLPHONE);

        willThrow(new OperationNotAllowedException("")).given(service).updatePhone(any(UUID.class), any(PhoneDTO.class));

        val json = objectMapper.writeValueAsString(phone);

        mvc.perform(put("/phones/" + phone.id().toString())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isForbidden());

    }


    @Test
    @SneakyThrows
    void shouldSearchPhones() {

        val phone = new PhoneDTO(UUID.randomUUID(), new UserDTO(UUID.randomUUID(),null, null, null),null,PhoneType.CELLPHONE);

        val message = objectMapper.writeValueAsString(phone);

        given(service.findAll(any(PhoneDTO.class), any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/phones?page=1&size=10")
                        .content(message)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldSearchPhonesBodyEmpty() {

        given(service.findAll(any(PhoneDTO.class), any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/phones?page=1&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should List All Phones sorted")
    void shouldGetAllPhonesSortedList() {

        given(service.findAll(any(PhoneDTO.class), any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/phones?sort=type|desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Sort sort = Sort.by(Sort.Order.desc("type"));

        verify(service).findAll(any(PhoneDTO.class), eq(PageRequest.of(0, 50, sort)));

    }

    @Test
    @SneakyThrows
    @DisplayName("Should delete phone by Id")
    void shouldDeletePhone() {

        mvc.perform(delete("/phones/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(any(UUID.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("Should not delete phone by Id - Phone Not Found")
    void shouldNotDeleteUserNotFound() {

        willThrow(PhoneNotFoundException.class).given(service).delete(any(UUID.class));

        mvc.perform(delete("/phones/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}