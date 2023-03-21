package test.com.pmrodrigues.users.service;

import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.users.clients.RoleClient;
import com.pmrodrigues.users.repositories.KeycloakUserRepository;
import com.pmrodrigues.users.repositories.UserRepository;
import com.pmrodrigues.users.service.RoleService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.pmrodrigues.users.specifications.SpecificationUser.externalId;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.data.jpa.domain.Specification.where;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestRoleService {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private KeycloakUserRepository keycloakUserRepository;

    @Mock
    private RoleClient roleClient;

    @Mock
    private UserRepository userRepository;


    @Test
    void shouldListUser() {

        val user = new UserRepresentation();
        val page = mock(Page.class);
        user.setId(UUID.randomUUID().toString());
        given(roleClient.getUsersInRole(Security.SYSTEM_ADMIN)).willReturn(ResponseEntity.of(Optional.of(List.of(user))));
        given(userRepository.findAll(where(externalId(List.of(UUID.fromString(user.getId())))),
                PageRequest.of(0,10))).willReturn(page);


        val returned = roleService.getUsersInRole(Security.SYSTEM_ADMIN, PageRequest.of(0,10));
        assertFalse(returned.isEmpty());

    }

    @Test
    void shouldNotFoundUser() {

        given(roleClient.getUsersInRole(Security.SYSTEM_ADMIN)).willReturn(ResponseEntity.of(Optional.of(List.of())));
        given(userRepository.findAll(where(externalId(List.of())),
                PageRequest.of(0,10))).willReturn(Page.empty());


        val returned =roleService.getUsersInRole(Security.SYSTEM_ADMIN, PageRequest.of(0,10));
        assertTrue(returned.isEmpty());


    }


}
