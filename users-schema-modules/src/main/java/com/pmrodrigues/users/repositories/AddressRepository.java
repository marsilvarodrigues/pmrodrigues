package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.Address;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface AddressRepository extends CrudRepository<Address, UUID> {
}
