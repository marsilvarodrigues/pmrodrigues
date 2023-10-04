package test.com.pmrodrigues.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.controlleradvices.DuplicatedKeyControllerAdvice;
import com.pmrodrigues.commons.controlleradvices.ValidationErrorControllerAdvice;
import com.pmrodrigues.commons.exceptions.NotFoundException;
import com.pmrodrigues.security.configurations.WebSecurityConfiguration;
import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.users.dtos.AddressDTO;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.exceptions.PhoneNotFoundException;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.rest.AddressController;
import com.pmrodrigues.users.service.AddressService;
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
        AddressController.class,
        ValidationErrorControllerAdvice.class,
        DuplicatedKeyControllerAdvice.class} )
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
class TestAddressController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mvc;

    @MockBean
    private AddressService service;


    @Test
    @SneakyThrows
    void shouldGetAddressById(){

        given(service.findById(any(UUID.class))).willReturn(Address.builder()
                .state(State.builder()
                        .build())
                .owner(User.builder().build())
                .build());

        mvc.perform(get(format("/addresses/%s",UUID.randomUUID()))
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldntGetAddressById() {
        given(service.findById(any(UUID.class))).willThrow(new NotFoundException());

        mvc.perform(get(format("/addresses/%s",UUID.randomUUID()))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldAddANewAddress() {


        val address = new AddressDTO(null, AddressType.STREET, "teste",null, "12345-123", "TESTE", "TESTE", "RJ",
                new UserDTO(UUID.randomUUID(), "teste", "teste", "test@test.com"));

        val json = objectMapper.writeValueAsString(address);
        val returned = new AddressDTO(UUID.randomUUID(),
                address.addressType(),
                address.address1(),
                address.address2(),
                address.zipcode(),
                address.neighbor(),
                address.city(),
                address.state(),
                address.owner());

        given(service.create(any(AddressDTO.class))).willReturn(returned);



        mvc.perform(post("/addresses")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/addresses/" + returned.id().toString()));
    }

    @Test
    @SneakyThrows
    void shouldntAddAddressUserNotAllowed() {

        val address = new AddressDTO(null, AddressType.STREET, "teste",null, "12345-123", "TESTE", "TESTE", "RJ",
                new UserDTO(UUID.randomUUID(), "teste", "teste", "test@test.com"));

        val json = objectMapper.writeValueAsString(address);

        given(service.create(any(AddressDTO.class))).willThrow(new OperationNotAllowedException("User not allowed for this operation"));

        mvc.perform(post("/addresses")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void shouldUpdateAddress(){

        val address = new AddressDTO(UUID.randomUUID(), AddressType.STREET, "teste",null, "12345-123", "TESTE", "TESTE", "RJ",
                new UserDTO(UUID.randomUUID(), "teste", "teste", "test@test.com"));

        willDoNothing().given(service).update(any(UUID.class), any(AddressDTO.class));

        val json = objectMapper.writeValueAsString(address);

        mvc.perform(put("/addresses/" + address.id())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldntUpdatAddressNotFound(){

        val address = new AddressDTO(UUID.randomUUID(), AddressType.STREET, "teste",null, "12345-123", "TESTE", "TESTE", "RJ",
                new UserDTO(UUID.randomUUID(), "teste", "teste", "test@test.com"));

        willThrow(new NotFoundException()).given(service).update(any(UUID.class), any(AddressDTO.class));

        val json = objectMapper.writeValueAsString(address);

        mvc.perform(put("/addresses/" + address.id().toString())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void shouldntUpdatAddressOperationNotAllowed(){

        val address = new AddressDTO(UUID.randomUUID(), AddressType.STREET, "teste",null, "12345-123", "TESTE", "TESTE", "RJ",
                new UserDTO(UUID.randomUUID(), "teste", "teste", "test@test.com"));

        willThrow(new OperationNotAllowedException("")).given(service).update(any(UUID.class), any(AddressDTO.class));

        val json = objectMapper.writeValueAsString(address);

        mvc.perform(put("/addresses/" + address.id().toString())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isForbidden());

    }


    @Test
    @SneakyThrows
    void shouldSearchAddress() {

        val address = new AddressDTO(UUID.randomUUID(), AddressType.STREET, "teste",null, "12345-123", "TESTE", "TESTE", "RJ",
                new UserDTO(UUID.randomUUID(), "teste", "teste", "test@test.com"));

        val message = objectMapper.writeValueAsString(address);

        when(service.findAll(any(AddressDTO.class), any(PageRequest.class))).thenReturn(Page.empty());

        mvc.perform(get("/addresses?page=1&size=10")
                        .content(message)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldSearchAddressBodyEmpty() {

        when(service.findAll(any(AddressDTO.class), any(PageRequest.class))).thenReturn(Page.empty());

        mvc.perform(get("/addresses?page=1&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should List All Addresses sorted")
    void shouldGetAllUsersSortedList() {

        given(service.findAll(any(AddressDTO.class), any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/addresses?sort=city|desc&sort=state.name|asc&sort=zipcode|desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Sort sort = Sort.by(Sort.Order.desc("city"),
                Sort.Order.asc("state.name"),
                Sort.Order.desc("zipcode"));

        verify(service).findAll(any(AddressDTO.class), eq(PageRequest.of(0, 50, sort)));

    }

    @Test
    @SneakyThrows
    @DisplayName("Should delete address by Id")
    void shouldDeleteUser() {


        mvc.perform(delete("/addresses/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(any(UUID.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("Should not delete user by Id - Address Not Found")
    void shouldNotDeleteUserNotFound() {

        willThrow(PhoneNotFoundException.class).given(service).delete(any(UUID.class));

        mvc.perform(delete("/addresses/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}