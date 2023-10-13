package com.pmrodrigues.users.service;

import com.pmrodrigues.commons.services.DataService;
import com.pmrodrigues.users.dtos.ClientDTO;
import com.pmrodrigues.users.repositories.ClientRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ClientDTO create(@NonNull ClientDTO entity) {
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void update(@NonNull UUID id, @NonNull ClientDTO entity) {

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
