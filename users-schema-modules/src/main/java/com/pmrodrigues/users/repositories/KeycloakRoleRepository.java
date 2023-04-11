package com.pmrodrigues.users.repositories;

import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.users.clients.RoleClient;
import com.pmrodrigues.users.dtos.RoleDTO;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
@RolesAllowed({Security.SYSTEM_ADMIN})
public class KeycloakRoleRepository {
    
    private final RoleClient roleClient;
    
    public List<RoleDTO> getRoles() {


        log.info("list all roles from system");
        val response = roleClient.getRoles();
        if( response.getStatusCode().is2xxSuccessful() ){
            return response.getBody()
                    .stream()
                    .map( roleRepresentation -> new RoleDTO(roleRepresentation.getId(),
                            roleRepresentation.getName()))
                    .sorted(Comparator.comparing(RoleDTO::name))
                    .collect(Collectors.toList());
        }
        return List.of();

    }
    
}
