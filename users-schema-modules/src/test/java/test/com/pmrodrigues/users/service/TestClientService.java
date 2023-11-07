package test.com.pmrodrigues.users.service;

import com.pmrodrigues.users.dtos.AddressDTO;
import com.pmrodrigues.users.dtos.ClientDTO;
import com.pmrodrigues.users.dtos.PhoneDTO;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.model.Client;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import com.pmrodrigues.users.model.enums.PhoneType;
import com.pmrodrigues.users.repositories.ClientRepository;
import com.pmrodrigues.users.service.ClientService;
import com.pmrodrigues.users.service.UserService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.verification.VerificationMode;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class TestClientService {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ClientService service;


    @Test
    void shouldAddClient() {

        val owner = new UserDTO(null, "test", "test", "test@email.com");
        val dto = new ClientDTO(null, "test", "test", "email@emai.com", LocalDate.now().minusYears(18),
                List.of(new AddressDTO(null, AddressType.STREET, "TESTE", null, "12345-123", "test", "test", "RJ", owner)),
                List.of(new PhoneDTO(null, owner, "12345-1234", PhoneType.CELLPHONE)));

        val client = dto.toClient().withId(UUID.randomUUID());

        given(userService.exist(anyString())).willReturn(Boolean.FALSE);
        given(clientRepository.save(any(Client.class))).willReturn(client);
        given(userService.generateUser(any(User.class))).willReturn(client);

        assertNotNull(service.create(dto));

    }

    @Test
    void shouldNotAddANewClientDuplicateKeyException(){
        val owner = new UserDTO(null, "test", "test", "test@email.com");
        val dto = new ClientDTO(null, "test", "test", "email@emai.com", LocalDate.now().minusYears(18),
                List.of(new AddressDTO(null, AddressType.STREET, "TESTE", null, "12345-123", "test", "test", "RJ", owner)),
                List.of(new PhoneDTO(null, owner, "12345-1234", PhoneType.CELLPHONE)));

        given(userService.exist(anyString())).willReturn(Boolean.TRUE);

        assertThrows(DuplicateKeyException.class, () -> service.create(dto));
    }

    @Test
    void shouldUpdate() {
        val id = UUID.randomUUID();
        val owner = new UserDTO(id, "test", "test", "test@email.com");
        val dto = new ClientDTO(id, "test", "test", "email@emai.com", LocalDate.now().minusYears(18),
                List.of(new AddressDTO(null, AddressType.STREET, "TESTE", null, "12345-123", "test", "test", "RJ", owner)),
                List.of(new PhoneDTO(null, owner, "12345-1234", PhoneType.CELLPHONE)));

        val client = dto.toClient();

        given(clientRepository.findById(any(UUID.class))).willReturn(Optional.of(client));
        willDoNothing().given(userService).update(client);
        given(clientRepository.save(any(Client.class))).willReturn(client);

        verify(clientRepository.save(any(Client.class)), times(1));
    }

}