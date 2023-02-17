package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.User;
import feign.Param;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface AddressRepository extends PagingAndSortingRepository<Address, UUID>, JpaSpecificationExecutor<Address> {

    Page<Address> findByOwner(@Param("owner") @NonNull final User owner, @NonNull PageRequest pageRequest);

}
