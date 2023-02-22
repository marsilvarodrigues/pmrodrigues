package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.Address;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface AddressRepository extends PagingAndSortingRepository<Address, UUID>, JpaSpecificationExecutor<Address> {

}
