package test.com.pmrodrigues.users.service;

import com.pmrodrigues.commons.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.dtos.PhoneDTO;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.exceptions.PhoneNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class TestPhoneService {

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
            given(userService.getById(any(UUID.class))).willReturn(defaultOwner);
            given(phoneRepository.save(any(Phone.class))).willReturn(Phone.builder()
                    .owner(User.builder().build())
                    .build());


            phoneService.create(new PhoneDTO(null,
                    new UserDTO(defaultOwner.getId(),null,null,null),
                    "teste", PhoneType.CELLPHONE));

            verify(phoneRepository, times(1)).save(any(Phone.class));
        }

    }

    @Test
    void shouldSaveMyPhoneWithoutOwnerOnMessage() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ){

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            given(phoneRepository.save(any(Phone.class))).willReturn(Phone.builder()
                    .owner(User.builder().build())
                    .build());
            phoneService.create(new PhoneDTO(null,
                    null,
                    "teste", PhoneType.CELLPHONE));

            verify(phoneRepository, times(1)).save(any(Phone.class));
        }
    }

    @Test
    void shouldNotSaveUserDifferentThanOwner() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ){

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            given(userService.getById(any(UUID.class))).willReturn(User.builder().id(UUID.randomUUID()).build());

            assertThrows(OperationNotAllowedException.class, (() -> phoneService.create(new PhoneDTO(null,
                    new UserDTO(UUID.randomUUID(),null,null,null),
                    "teste", PhoneType.CELLPHONE))));
        }
    }

    @Test
    void shouldSaveUserDifferentThanOwner() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ){

            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);
            given(userService.getById(any(UUID.class))).willReturn(User.builder().id(UUID.randomUUID()).build());
            given(phoneRepository.save(any(Phone.class))).willReturn(Phone.builder()
                    .owner(User.builder().build())
                    .build());
            phoneService.create(new PhoneDTO(null,
                    new UserDTO(UUID.randomUUID(),null,null,null),
                    "teste", PhoneType.CELLPHONE));

            verify(phoneRepository, times(1)).save(any(Phone.class));
        }
    }

    @Test
    void shouldUpdatePhone() {

        try(val mockStatic = mockStatic(SecurityUtils.class) ){
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            given(userService.getById(any(UUID.class))).willReturn(defaultOwner);
            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone()));

            val id = UUID.randomUUID();

            phoneService.update(id, new PhoneDTO(id,
                    new UserDTO(defaultOwner.getId(),null,null,null),
                    "teste", PhoneType.CELLPHONE));

            verify(phoneRepository, times(1)).save(any(Phone.class));
        }

    }

    @Test
    void shouldSaveForOtherUserLoggedUserIsSystemAdmin(){
        try(val mockStatic = mockStatic(SecurityUtils.class) ){
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);
            given(userService.getById(any(UUID.class))).willReturn(new User().withId(UUID.randomUUID()));
            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone()));

            val id = UUID.randomUUID();

            phoneService.update(id, new PhoneDTO(id,
                    new UserDTO(defaultOwner.getId(),null,null,null),
                    "teste", PhoneType.CELLPHONE));

            verify(phoneRepository, times(1)).save(any(Phone.class));
        }
    }

    @Test
    void shouldNotSaveUserIsNotSystemAdmin() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ){
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            given(userService.getById(any(UUID.class))).willReturn(new User().withId(UUID.randomUUID()));
            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone()));

            val id = UUID.randomUUID();

            assertThrows(OperationNotAllowedException.class, () -> phoneService.update(id, new PhoneDTO(id,
                    new UserDTO(defaultOwner.getId(),null,null,null),
                    "teste", PhoneType.CELLPHONE)));


        }
    }
    @Test
    void phoneNotFound() {
        given(userService.getById(any(UUID.class))).willReturn(new User().withId(UUID.randomUUID()));
        given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        val id = UUID.randomUUID();

        assertThrows(PhoneNotFoundException.class, () -> phoneService.update(id, new PhoneDTO(id,
                new UserDTO(defaultOwner.getId(),null,null,null),
                "teste", PhoneType.CELLPHONE)));
    }

    @Test
    void shouldListOnlyMyPhone() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);
            given(phoneRepository.findAll(any(Specification.class),any(PageRequest.class))).willReturn(Page.empty());
            val pageable = mock(PageRequest.class);
            phoneService.findAll(new PhoneDTO(null, new UserDTO(null, null, null, null), null, null), pageable);

            verify(phoneRepository).findAll(
                    any(Specification.class),any(PageRequest.class));
        }
    }

    @Test
    void shouldListAllMyPhone() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);
            given(phoneRepository.findAll(any(Specification.class),any(PageRequest.class))).willReturn(Page.empty());
            val pageable = mock(PageRequest.class);
            phoneService.findAll(new PhoneDTO(null, new UserDTO(null, null, null, null), null, null), pageable);

            verify(phoneRepository).findAll(
                    any(Specification.class),any(PageRequest.class));
        }
    }

    @Test
    void shouldGetPhoneById() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone().withOwner(new User())));

            assertNotNull(phoneService.findById(UUID.randomUUID()));

        }
    }

    @Test
    void shouldGetMyPhoneById() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone().withOwner(defaultOwner)));

            assertNotNull(phoneService.findById(UUID.randomUUID()));

        }
    }

    @Test
    void shouldNotGetPhoneByIdPhoneNotFound() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            assertThrows(PhoneNotFoundException.class, () ->phoneService.findById(UUID.randomUUID()));

        }
    }

    @Test
    void shouldNotGetMyPhoneByIdPhoneNotFound() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone().withOwner(new User())));

            assertThrows(PhoneNotFoundException.class, () ->phoneService.findById(UUID.randomUUID()));

        }
    }

    @Test
    void shouldDeleteAnyPhone() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.TRUE);

            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone()));

            phoneService.delete(UUID.randomUUID());
            verify(phoneRepository, times(1)).delete(any(Phone.class));

        }
    }

    @Test
    void shouldDeleteMyPhone() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone().withOwner(defaultOwner)));

            phoneService.delete(UUID.randomUUID());
            verify(phoneRepository, times(1)).delete(any(Phone.class));

        }
    }

    @Test
    void shouldNotDeleteMyPhone() {
        try(val mockStatic = mockStatic(SecurityUtils.class) ) {
            mockStatic.when(() -> SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)).thenReturn(Boolean.FALSE);

            given(phoneRepository.findById(any(UUID.class))).willReturn(Optional.of(new Phone().withOwner(new User())));

            assertThrows(PhoneNotFoundException.class, () -> phoneService.delete(UUID.randomUUID()));


        }
    }
}
