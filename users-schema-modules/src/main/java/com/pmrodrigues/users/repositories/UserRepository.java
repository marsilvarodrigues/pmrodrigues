package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, UUID> , JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(@Param("email") String email);

    Optional<User> findByExternalId(@Param("externalId") UUID externalId);

}
