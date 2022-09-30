package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.State;
import lombok.NonNull;
import org.jboss.resteasy.annotations.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends CrudRepository<State, UUID> {

    @Override
    @Query
    public Iterable<State> findAll();

    public Optional<State> findByCode(@NonNull final String code);
}
