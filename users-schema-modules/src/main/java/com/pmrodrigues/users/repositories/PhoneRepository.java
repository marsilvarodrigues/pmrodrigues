package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.Phone;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RepositoryRestResource(exported = false)
public interface PhoneRepository extends PagingAndSortingRepository<Phone, UUID>, JpaSpecificationExecutor<Phone> {
}
