package test.com.pmrodrigues.users.service;

import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.dtos.PhoneDTO;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.model.Phone;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.PhoneType;
import com.pmrodrigues.users.repositories.PhoneRepository;
import com.pmrodrigues.users.service.PhoneService;
import com.pmrodrigues.users.service.UserService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestPhoneService {

    @Mock
    private UserService userService;

    @Mock
    private PhoneRepository phoneRepository;

    @InjectMocks
    private PhoneService phoneService;

    private User defaultOwner = User.builder().id(UUID.randomUUID())
            .email("teste@teste.com")
            .build();

    @BeforeEach
    public void beforeEach(){

        given(userService.getAuthenticatedUser()).willReturn(Optional.of(defaultOwner));

    }

    @Test
    void shouldSaveMyPhone() {

        try(val mockStatic = mockStatic(SecurityUtils.class) ){

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            given(userService.findById(any(UUID.class))).willReturn(defaultOwner);

            phoneService.createNewPhone(new PhoneDTO(null,
                    new UserDTO(defaultOwner.getId(),null,null,null),
                    "teste", PhoneType.CELLPHONE));

            verify(phoneRepository, times(1)).save(any(Phone.class));
        }

    }

    @Test
    void shouldSaveMyPhoneWithoutOwnerOnMessage() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ){

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

            phoneService.createNewPhone(new PhoneDTO(null,
                    null,
                    "teste", PhoneType.CELLPHONE));

            verify(phoneRepository, times(1)).save(any(Phone.class));
        }
    }

    @Test
    void shouldNotSaveUserDifferentThanOwner() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ){

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            given(userService.findById(any(UUID.class))).willReturn(User.builder().id(UUID.randomUUID()).build());

            assertThrows(OperationNotAllowedException.class, (() -> phoneService.createNewPhone(new PhoneDTO(null,
                    new UserDTO(UUID.randomUUID(),null,null,null),
                    "teste", PhoneType.CELLPHONE))));
        }
    }

    @Test
    void shouldSaveUserDifferentThanOwner() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ){

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);
            given(userService.findById(any(UUID.class))).willReturn(User.builder().id(UUID.randomUUID()).build());

            phoneService.createNewPhone(new PhoneDTO(null,
                    new UserDTO(UUID.randomUUID(),null,null,null),
                    "teste", PhoneType.CELLPHONE));

            verify(phoneRepository, times(1)).save(any(Phone.class));
        }
    }
}
