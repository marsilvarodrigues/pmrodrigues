package test.com.pmrodrigues.users.service;

import com.pmrodrigues.users.dtos.ClientDTO;
import com.pmrodrigues.users.model.Client;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class TestClientService {

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private ClientService service;

    private UserService userService;

    @Test
    void shouldAddClient() {

        val dto = new ClientDTO(null, null, null, "email@emai.com", null, null, null);
        given(repository.save(any(Client.class))).willReturn(new Client());



    }

}