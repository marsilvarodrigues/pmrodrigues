package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.State;
import lombok.NonNull;
import org.jboss.resteasy.annotations.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RepositoryRestResource(path = "/states", exported = false)
public interface StateRepository extends CrudRepository<State, UUID> {

    @Override
    @Query
    @RestResource
    Iterable<State> findAll();

    @RestResource
    Optional<State> findByCode(@NonNull String code);
}
