package test.com.pmrodrigues.users.service;

import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.exceptions.AddressNotFoundException;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.service.AddressService;
import com.pmrodrigues.users.service.UserService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class TestAddressService {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AddressService service;

    private User defaultOwner = User.builder().id(UUID.randomUUID())
            .email("teste@teste.com")
            .build();

    @BeforeEach
    public void beforeEach(){

        given(userService.getAuthenticatedUser()).willReturn(Optional.of(defaultOwner));

    }

    @Test
    void shouldAddMyNewAddress() {

        val toSave = mock(Address.class);
        val saved = service.createNewAddress(toSave);
        verify(toSave, times(1)).setOwner(any(User.class));
        verify(addressRepository, times(1)).save(toSave);

    }

    @Test
    void shouldAddANewAddressForOtherUser() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

        val toSave = mock(Address.class);
        given(toSave.getOwner()).willReturn(defaultOwner);
        val saved = service.createNewAddress(toSave);
        verify(toSave, never()).setOwner(any(User.class));
        verify(addressRepository, times(1)).save(toSave);

        mockStatic.close();
    }

    @Test
    void shouldOnlyAdministratorCouldCreateAddressForOwnerPreviouslySettingUp() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

        val other = User.builder().id(UUID.randomUUID())
                .email("other@teste.com")
                .build();
        val toSave = mock(Address.class);
        given(toSave.getOwner()).willReturn(other);
        val saved = service.createNewAddress(toSave);
        verify(toSave, never()).setOwner(any(User.class));
        verify(addressRepository, times(1)).save(toSave);

        mockStatic.close();

    }

    @Test
    void shouldNotAdd() {

        val mockStatic = mockStatic(SecurityUtils.class);
        try {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            val other = User.builder().id(UUID.randomUUID())
                    .email("other@teste.com")
                    .build();
            val toSave = mock(Address.class);
            given(toSave.getOwner()).willReturn(other);

            assertThrows(OperationNotAllowedException.class, () ->{
                val saved = service.createNewAddress(toSave);
            });

        }finally {
            mockStatic.close();
        }


    }


    @Test
    @SneakyThrows
    void shouldUpdateAddress() {

        val toUpdate = mock(Address.class);
        given(toUpdate.getId()).willReturn(UUID.randomUUID());
        given(toUpdate.getOwner()).willReturn(defaultOwner);
        given(addressRepository.findById(any(UUID.class))).willReturn(
                Optional.of(Address.builder().owner(defaultOwner).build())
        );

        service.updateAddress(toUpdate);

        verify(addressRepository, times(1)).save(any(Address.class));

    }

    @Test
    void shouldNotUpdateAddressNotFoundError() {

        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.empty());
        val address = Address.builder().id(UUID.randomUUID()).build();
        assertThrows(AddressNotFoundException.class, () ->
           service.updateAddress(address)
        );
    }

    @Test
    @SneakyThrows
    void shouldOnlyOwnerUpdateAddress() {
        val toUpdate = mock(Address.class);
        given(toUpdate.getId()).willReturn(UUID.randomUUID());
        given(toUpdate.getOwner()).willReturn(defaultOwner);


        given(addressRepository.findById(any(UUID.class)))
                .willReturn(Optional.of(Address.builder().owner(defaultOwner).build()));

        service.updateAddress(toUpdate);
        verify(addressRepository, times(1)).save(any(Address.class));

    }

    @Test
    void shouldNotSaveOwnerDifferent() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

        val toUpdate = mock(Address.class);
        val differentOwnser = User.builder().id(UUID.randomUUID()).build();

        given(toUpdate.getId()).willReturn(UUID.randomUUID());
        given(toUpdate.getOwner()).willReturn(differentOwnser);

        given(addressRepository.findById(any(UUID.class)))
                .willReturn(Optional.of(Address.builder().owner(defaultOwner).build()));

        assertThrows(OperationNotAllowedException.class, () -> service.updateAddress(toUpdate));

        mockStatic.close();

    }

    @Test
    void shouldListMyAddresses() {
        val addresses = mock(List.class);
        val pageRequest = mock(PageRequest.class);

        given(addressRepository.findByOwner(any(User.class)))
                .willReturn(addresses);

        val pages = service.listMyAddress();

        assertNotNull(addresses);
    }

 
}