package test.com.pmrodrigues.users.service;

import com.pmrodrigues.commons.exceptions.NotFoundException;
import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.dtos.AddressDTO;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.exceptions.AddressNotFoundException;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.repositories.StateRepository;
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
    private StateRepository stateRepository;

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

        val toSave = mock(AddressDTO.class);
        val state = Optional.of(new State());
        val address = mock(Address.class);

        given(toSave.state()).willReturn("RJ");
        given(stateRepository.findByCode(anyString())).willReturn(state);
        given(toSave.toAddress()).willReturn(address);
        given(address.withState(any(State.class))).willReturn(address);

        service.createNewAddress(toSave);
        verify(address, times(1)).setOwner(any(User.class));
        verify(addressRepository, times(1)).save(any(Address.class));

    }

    @Test
    void shouldAddANewAddressForOtherUser() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

        val toSave = mock(AddressDTO.class);
        val state = Optional.of(new State());
        val address = mock(Address.class);

        given(toSave.state()).willReturn("RJ");
        given(stateRepository.findByCode(anyString())).willReturn(state);
        given(toSave.toAddress()).willReturn(address);
        given(address.withState(any(State.class))).willReturn(address);

        given(address.getOwner()).willReturn(defaultOwner);
        service.createNewAddress(toSave);
        verify(address, never()).setOwner(any(User.class));
        verify(addressRepository, times(1)).save(address);

        mockStatic.close();
    }

    @Test
    void shouldOnlyAdministratorCouldCreateAddressForOwnerPreviouslySettingUp() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

        val other = User.builder().id(UUID.randomUUID())
                .email("other@teste.com")
                .build();

        val toSave = mock(AddressDTO.class);
        val state = Optional.of(new State());
        val address = mock(Address.class);

        given(toSave.state()).willReturn("RJ");
        given(stateRepository.findByCode(anyString())).willReturn(state);
        given(toSave.toAddress()).willReturn(address);
        given(address.withState(any(State.class))).willReturn(address);

        given(address.getOwner()).willReturn(other);
        service.createNewAddress(toSave);
        verify(address, never()).setOwner(any(User.class));
        verify(addressRepository, times(1)).save(address);

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


            val toSave = mock(AddressDTO.class);
            val state = Optional.of(new State());
            val address = mock(Address.class);

            given(toSave.state()).willReturn("RJ");
            given(stateRepository.findByCode(anyString())).willReturn(state);
            given(toSave.toAddress()).willReturn(address);
            given(address.withState(any(State.class))).willReturn(address);

            given(address.getOwner()).willReturn(other);

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

        val mockStatic = mockStatic(SecurityUtils.class);
        try{

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

            val toUpdate = mock(AddressDTO.class);
            given(toUpdate.id()).willReturn(UUID.randomUUID());
            given(toUpdate.owner()).willReturn(UserDTO.fromUser(defaultOwner));
            given(toUpdate.state()).willReturn("RJ");
            given(stateRepository.findByCode(anyString())).willReturn(Optional.of(new State()));
            given(addressRepository.findById(any(UUID.class))).willReturn(
                    Optional.of(Address.builder().owner(defaultOwner).build())
            );

            service.updateAddress(UUID.randomUUID(), toUpdate);

            verify(addressRepository, times(1)).save(any(Address.class));
        }finally {
            mockStatic.close();
        }

    }

    @Test
    void shouldNotUpdateAddressNotFoundError() {

        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.empty());
        val address = mock(AddressDTO.class);
        assertThrows(AddressNotFoundException.class, () ->
           service.updateAddress(UUID.randomUUID(), address)
        );
    }

    @Test
    @SneakyThrows
    void shouldOnlyOwnerUpdateAddress() {

        val mockStatic = mockStatic(SecurityUtils.class);
        try {

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

            val toUpdate = mock(AddressDTO.class);
            given(toUpdate.id()).willReturn(UUID.randomUUID());
            given(toUpdate.owner()).willReturn(UserDTO.fromUser(defaultOwner));
            given(toUpdate.state()).willReturn("RJ");
            given(stateRepository.findByCode(anyString())).willReturn(Optional.of(new State()));


            given(addressRepository.findById(any(UUID.class)))
                    .willReturn(Optional.of(Address.builder().owner(defaultOwner).build()));

            service.updateAddress(UUID.randomUUID(), toUpdate);
            verify(addressRepository, times(1)).save(any(Address.class));
        }finally {
            mockStatic.close();
        }

    }

    @Test
    void shouldNotSaveOwnerDifferent() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

        val toUpdate = mock(AddressDTO.class);
        val differentOwnser = User.builder().id(UUID.randomUUID()).build();
        given(toUpdate.id()).willReturn(UUID.randomUUID());
        given(toUpdate.owner()).willReturn(UserDTO.fromUser(differentOwnser));
        given(toUpdate.state()).willReturn("RJ");
        given(stateRepository.findByCode(anyString())).willReturn(Optional.of(new State()));

        given(addressRepository.findById(any(UUID.class)))
                .willReturn(Optional.of(Address.builder().owner(defaultOwner).build()));

        assertThrows(OperationNotAllowedException.class, () -> service.updateAddress(UUID.randomUUID(), toUpdate));

        mockStatic.close();

    }

    @Test
    void shouldGetAddressById() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.of(new Address()));
        assertNotNull(service.findById(UUID.randomUUID()));

        mockStatic.close();
    }

    @Test
    void shouldGetAddressByIdOnlyMyAddress() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
        val address = Address.builder().owner(defaultOwner).build();
        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.of(address));
        assertNotNull(service.findById(UUID.randomUUID()));

        mockStatic.close();
    }

    @Test
    void shouldntFindAddressById() {
        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(UUID.randomUUID()));

        mockStatic.close();
    }

    @Test
    void shouldntFindAddressByIdDifferentOwner() {
        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
        val otherOwner = User.builder().id(UUID.randomUUID()).build();
        val address = Address.builder().owner(otherOwner).build();

        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.of(address));

        assertThrows(NotFoundException.class, () -> service.findById(UUID.randomUUID()));

        mockStatic.close();
    }

    @Test
    void shouldDeleteAddress() {

        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

        val address = Address.builder().id(UUID.randomUUID()).owner(defaultOwner).build();

        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.of(address));

        service.delete(address);

        verify(addressRepository).delete(address);

        mockStatic.close();

    }

    @Test
    void shouldDeleteOnlyMyAddress() {
        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

        val address = Address.builder().id(UUID.randomUUID()).owner(defaultOwner).build();

        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.of(address));

        service.delete(address);

        verify(addressRepository).delete(address);

        mockStatic.close();
    }

    @Test
    void shouldntDeleteOtherAddress() {
        val mockStatic = mockStatic(SecurityUtils.class);
        mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

        val otherOwner = User.builder().id(UUID.randomUUID()).email("other@teste.com").build();
        val address = Address.builder().id(UUID.randomUUID()).owner(otherOwner).build();

        given(addressRepository.findById(any(UUID.class))).willReturn(Optional.of(address));

        assertThrows(OperationNotAllowedException.class, () -> service.delete(address));


        mockStatic.close();
    }

 
}