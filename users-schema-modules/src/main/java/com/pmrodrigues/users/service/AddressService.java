package com.pmrodrigues.users.service;

import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.exceptions.AddressNotFoundException;
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

import static org.springframework.beans.BeanUtils.copyProperties;

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
        val connectedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);

        log.info("create a new address {}", address);

        if(address.getOwner() == null ) {
            address.setOwner(connectedUser);
        } else if(!address.getOwner().equals(connectedUser) && !SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)) {
            throw new OperationNotAllowedException("User not allowed for this operation");
        }
        return repository.save(address);
    }

    @Timed(histogram = true, value= "AddressService.updateAddress")
    public void updateAddress(@NonNull Address address) throws OperationNotAllowedException {
        log.info("update the address {}", address);

        val existed = repository.findById(address.getId()).orElseThrow(AddressNotFoundException::new);

        if(existed.getOwner().equals(address.getOwner()) || SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)) {
            copyProperties(existed, address, "id", "createdAt", "updateAt", "createdBy", "updateBy");
            repository.save(existed);
        } else {
            throw new OperationNotAllowedException("User not allowed for this operation");
        }

    }
}
