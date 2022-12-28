package com.pmrodrigues.users.service;

import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.repositories.AddressRepository;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
@Component
public class AddressService {
    private final AddressRepository repository;

    private final UserService userService;

    @Timed(histogram = true, value = "AddressService.createNewAddress")
    @Transactional(propagation = Propagation.REQUIRED)
    @SneakyThrows
    public Address createNewAddress(@NonNull Address address){
        log.info("create a new address {} for user {}", address , null);
        val connectedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);
        if(address.getOwner() == null ) {
            address.setOwner(connectedUser);
        } else if(!address.getOwner().equals(connectedUser) && !SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)) {
            throw new OperationNotAllowedException("User not allowed for this operation");
        }
        return repository.save(address);
    }

}
