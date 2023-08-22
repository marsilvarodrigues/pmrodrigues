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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.pmrodrigues.users.specifications.SpecificationAddress.*;
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
    @Transactional(propagation = Propagation.REQUIRED)
    @SneakyThrows
    public void updateAddress(@NonNull UUID id, @NonNull Address address) throws OperationNotAllowedException {
        log.info("update the address {}", address);

        val existed = repository.findById(id)
                .orElseThrow(AddressNotFoundException::new);

        if(existed.getOwner().equals(address.getOwner()) || SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)) {
            copyProperties(address, existed, "id", "createdAt", "updateAt", "createdBy", "updateBy");
            repository.save(existed);
        } else {
            throw new OperationNotAllowedException("User not allowed for this operation");
        }

    }

    @Timed(histogram = true, value = "AddressService.findAll")
    @SneakyThrows
    public Page<Address> findAll(@NonNull Address address, @NonNull PageRequest pageRequest){
        log.info("list all addresses by sample {}", address);
        var loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);

        if( SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ){
            return repository.findAll(
                            state(address.getState())
                            .and(zipcode(address.getZipcode()))
                            .and(city(address.getCity()))
                            .and(neightboor(address.getNeighbor()))
                            .and(address(address.getAddress1())), pageRequest);
        } else {
            return repository.findAll(
                    owner(loggedUser)
                    .and(state(address.getState()))
                    .and(zipcode(address.getZipcode()))
                    .and(city(address.getCity()))
                    .and(neightboor(address.getNeighbor()))
                    .and(address(address.getAddress1())), pageRequest);
        }

    }

    @Timed(histogram = true, value = "AddressService.findById")
    @SneakyThrows
    public Address findById(UUID id) {
        log.info("try to get address by id {}",id);
        var loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);
        if( SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ) {
            return repository.findById(id)
                    .orElseThrow(AddressNotFoundException::new);
        }else{
            val address =  repository.findById(id)
                    .orElseThrow(AddressNotFoundException::new);

            if( address.getOwner().equals(loggedUser) ) {
                return address;
            }else{
                throw new AddressNotFoundException();
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Timed(histogram = true, value = "AddressService.delete")
    @SneakyThrows
    public void delete(Address address) {
        log.info("try to delete address {}",address);

        val toDelete = repository.findById(address.getId())
                .orElseThrow(AddressNotFoundException::new);

        if( SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ) {
            repository.delete(toDelete);
        }else{
            val loggedUser = userService.getAuthenticatedUser()
                    .orElseThrow(UserNotFoundException::new);

            if( toDelete.getOwner().equals(loggedUser) ){
                repository.delete(toDelete);
            }else{
                throw new OperationNotAllowedException("User not allowed for this operation");
            }
        }

    }
}
