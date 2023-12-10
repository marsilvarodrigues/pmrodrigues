package com.pmrodrigues.users.service;

import com.pmrodrigues.commons.services.DataService;
import com.pmrodrigues.users.dtos.AddressDTO;
import com.pmrodrigues.users.dtos.ClientDTO;
import com.pmrodrigues.users.dtos.PhoneDTO;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.Client;
import com.pmrodrigues.users.repositories.ClientRepository;
import com.pmrodrigues.users.repositories.KeycloakUserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
@Component
public class ClientService implements DataService<UUID, ClientDTO> {

    private final ClientRepository repository;

    private final UserService userService;

    private final KeycloakUserRepository keycloakUserRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ClientDTO create(@NonNull ClientDTO entity) {
        log.info("creating a new client {}", entity);

        if(userService.exist(entity.email())){
            throw new DuplicateKeyException("I´m sorry but this was used before");
        }

        val client = repository.save(entity.toClient());

        return ClientDTO.fromClient((Client)userService.generateUser(client));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void update(@NonNull UUID id, @NonNull ClientDTO clientDTO) {
        log.info("trying to update client {}",clientDTO);
        val existed = repository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        existed.setEmail(clientDTO.email());
        existed.setFirstName(clientDTO.firstName());
        existed.setLastName(clientDTO.lastName());
        existed.setBirthday(clientDTO.birthDate());
        existed.setAddresses(clientDTO.addresses().stream().map(AddressDTO::toAddress).toList());
        existed.setPhones(clientDTO.phones().stream().map(PhoneDTO::toPhone).toList());

        keycloakUserRepository.update(existed);
        repository.save(existed);

    }

    @Override
    public Page<ClientDTO> findAll(@NonNull ClientDTO entity, @NonNull PageRequest pageRequest) {
        return null;
    }

    @Override
    public ClientDTO findById(UUID id) {
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(@NonNull UUID id) {

    }
}
