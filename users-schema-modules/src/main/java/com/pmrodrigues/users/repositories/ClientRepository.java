package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.Client;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RepositoryRestResource(exported = false)
public interface ClientRepository extends PagingAndSortingRepository<Client, UUID> {
}
