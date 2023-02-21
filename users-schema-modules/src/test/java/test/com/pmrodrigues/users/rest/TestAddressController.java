package test.com.pmrodrigues.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.controlleradvices.DuplicatedKeyControllerAdvice;
import com.pmrodrigues.commons.controlleradvices.ValidationErrorControllerAdvice;
import com.pmrodrigues.commons.exceptions.NotFoundException;
import com.pmrodrigues.security.configurations.WebSecurityConfiguration;
import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.users.dtos.AddressDTO;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
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

        given(service.getByID(any(UUID.class))).willReturn(Address.builder().build());

        mvc.perform(get(format("/addresses/%s",UUID.randomUUID()))
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldntGetAddressById() {
        given(service.getByID(any(UUID.class))).willThrow(new NotFoundException());

        mvc.perform(get(format("/addresses/%s",UUID.randomUUID()))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldAddANewAddress() {

        val state = State.builder().id(UUID.randomUUID()).code("RJ").name("TESTE").build();
        val address = Address.builder()
                .state(state)
                .address1("TESTE")
                .addressType(AddressType.STREET)
                .city("TESTE")
                .zipcode("TESTE")
                .neightboor("TESTE")
                .build();
        val json = objectMapper.writeValueAsString(address);
        address.setId(UUID.randomUUID());
        given(service.createNewAddress(any(Address.class))).willReturn(address);



        mvc.perform(post("/addresses")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/address/" + address.getId().toString()));
    }

    @Test
    @SneakyThrows
    void shouldntAddAddressUserNotAllowed() {

        val state = State.builder().id(UUID.randomUUID()).code("RJ").name("TESTE").build();
        val address = Address.builder()
                .state(state)
                .address1("TESTE")
                .addressType(AddressType.STREET)
                .city("TESTE")
                .zipcode("TESTE")
                .neightboor("TESTE")
                .build();
        val json = objectMapper.writeValueAsString(address);
        address.setId(UUID.randomUUID());
        given(service.createNewAddress(address)).willThrow(new OperationNotAllowedException("User not allowed for this operation"));

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

        val state = State.builder().id(UUID.randomUUID()).code("RJ").name("TESTE").build();
        val address = Address.builder()
                .state(state)
                .address1("TESTE")
                .addressType(AddressType.STREET)
                .city("TESTE")
                .zipcode("TESTE")
                .neightboor("TESTE")
                .id(UUID.randomUUID())
                .build();

        willDoNothing().given(service).updateAddress(any(UUID.class), any(Address.class));

        val json = objectMapper.writeValueAsString(address);

        mvc.perform(put("/addresses/" + address.getId().toString())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldntUpdatAddressNotFound(){

        val state = State.builder().id(UUID.randomUUID()).code("RJ").name("TESTE").build();
        val address = Address.builder()
                .state(state)
                .address1("TESTE")
                .addressType(AddressType.STREET)
                .city("TESTE")
                .zipcode("TESTE")
                .neightboor("TESTE")
                .id(UUID.randomUUID())
                .build();

        willThrow(new NotFoundException()).given(service).updateAddress(any(UUID.class), any(Address.class));

        val json = objectMapper.writeValueAsString(address);

        mvc.perform(put("/addresses/" + address.getId().toString())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    @SneakyThrows
    void shouldntUpdatAddressOperationNotAllowed(){

        val state = State.builder().id(UUID.randomUUID()).code("RJ").name("TESTE").build();
        val address = Address.builder()
                .state(state)
                .address1("TESTE")
                .addressType(AddressType.STREET)
                .city("TESTE")
                .zipcode("TESTE")
                .neightboor("TESTE")
                .id(UUID.randomUUID())
                .build();

        willThrow(new OperationNotAllowedException("")).given(service).updateAddress(any(UUID.class), any(Address.class));

        val json = objectMapper.writeValueAsString(address);

        mvc.perform(put("/addresses/" + address.getId().toString())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isForbidden());

    }


    @Test
    @SneakyThrows
    void shouldSearchAddress() {


        val state = State.builder().id(UUID.randomUUID()).build();
        val address = AddressDTO.builder()
                .state(state)
                .address1("TESTE")
                .addressType(AddressType.STREET)
                .city("TESTE")
                .zipcode("TESTE")
                .neightboor("TESTE")
                .build();

        val message = objectMapper.writeValueAsString(address);

        mvc.perform(get("/addresses?page=1&size=10")
                        .content(message)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    void shouldSearchAddressBodyEmpty() {

        mvc.perform(get("/addresses?page=1&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should List All Addresses sorted")
    void shouldGetAllUsersSortedList() {

        given(service.findAll(any(Address.class), any(PageRequest.class))).willReturn(Page.empty());

        mvc.perform(get("/addresses?sort=city|desc&sort=state.name|asc&sort=zipcode|desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Sort sort = Sort.by(Sort.Order.desc("city"),
                Sort.Order.asc("state.name"),
                Sort.Order.desc("zipcode"));

        verify(service).findAll(any(Address.class), eq(PageRequest.of(0, 50, sort)));

    }

}