package com.pmrodrigues.users.service;

import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.dtos.AddressDTO;
import com.pmrodrigues.users.exceptions.AddressNotFoundException;
import com.pmrodrigues.users.exceptions.StateNotFoundException;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.repositories.AddressRepository;
import com.pmrodrigues.users.repositories.StateRepository;
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

@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
@Component
public class AddressService {
    private final StateRepository stateRepository;
    private final AddressRepository repository;

    private final UserService userService;

    @Timed(histogram = true, value = "AddressService.createNewAddress")
    @Transactional(propagation = Propagation.REQUIRED)
    @SneakyThrows
    public Address createNewAddress(@NonNull AddressDTO address){
        val connectedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);

        val state = stateRepository.findByCode(address.state())
                .orElseThrow(StateNotFoundException::new);

        val toSave = address.toAddress().withState(state);

        log.info("create a new address {}", address);

        if(toSave.getOwner() == null ) {
            toSave.setOwner(connectedUser);
        } else if(!toSave.getOwner().equals(connectedUser) && !SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)) {
            throw new OperationNotAllowedException();
        }
        return repository.save(toSave);
    }

    @Timed(histogram = true, value= "AddressService.updateAddress")
    @Transactional(propagation = Propagation.REQUIRED)
    @SneakyThrows
    public void updateAddress(@NonNull UUID id, @NonNull AddressDTO address) throws OperationNotAllowedException {
        log.info("update the address {}", address);

        var existed = repository.findById(id)
                .orElseThrow(AddressNotFoundException::new);

        val state = stateRepository.findByCode(address.state())
                .orElseThrow(StateNotFoundException::new);

        val owner = address.owner().toUser();
        if(existed.getOwner().equals(owner) || SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)) {

            existed = existed.withOwner(owner)
                             .withAddress1(address.address1())
                             .withAddress2(address.address2())
                             .withAddressType(address.addressType())
                             .withState(state)
                             .withCity(address.city())
                             .withNeighbor(address.neighbor());

            repository.save(existed);
        } else {
            throw new OperationNotAllowedException();
        }

    }

    @Timed(histogram = true, value = "AddressService.findAll")
    @SneakyThrows
    public Page<Address> findAll(@NonNull AddressDTO address, @NonNull PageRequest pageRequest){
        log.info("list all addresses by sample {}", address);
        var loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);

        if( SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ){
            return repository.findAll(
                            owner(address.owner().toUser())
                            .and(state(address.toAddress().getState()))
                            .and(zipcode(address.zipcode()))
                            .and(city(address.city()))
                            .and(neighbor(address.neighbor()))
                            .and(address(address.address1())), pageRequest);
        } else {
            return repository.findAll(
                    owner(loggedUser)
                    .and(state(address.toAddress().getState()))
                    .and(zipcode(address.zipcode()))
                    .and(city(address.city()))
                    .and(neighbor(address.neighbor()))
                    .and(address(address.address1())), pageRequest);
        }

    }

    @Timed(histogram = true, value = "AddressService.findById")
    @SneakyThrows
    public Address findById(UUID id) {
        log.info("try to get address by id {}",id);
        var loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);
        val address =  repository.findById(id)
                .orElseThrow(AddressNotFoundException::new);

        if( SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ) {
            return address;
        }else{
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
    public void delete(@NonNull UUID id) {
        log.info("try to delete address {}",id);

        val address = repository.findById(id)
                .orElseThrow(AddressNotFoundException::new);

        if( SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ) {
            repository.delete(address);
        }else{
            val loggedUser = userService.getAuthenticatedUser()
                    .orElseThrow(UserNotFoundException::new);

            if( address.getOwner().equals(loggedUser) ){
                repository.delete(address);
            }else{
                throw new OperationNotAllowedException();
            }
        }

    }
}
