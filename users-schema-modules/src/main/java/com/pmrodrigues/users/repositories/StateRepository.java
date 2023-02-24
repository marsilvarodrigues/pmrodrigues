package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.State;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import org.jboss.resteasy.annotations.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RepositoryRestResource(collectionResourceRel = "states", path = "states")
public interface StateRepository extends CrudRepository<State, UUID> {

    @Override
    @Query
    @Timed(value = "StateRepository.findAll", histogram = true)
    @ApiOperation(value = "List all States", nickname = "listAll", response = State.class, tags={"state"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List all states", response = State.class),
            })
    Iterable<State> findAll();


    @Timed(value = "StateRepository.findByCode", histogram = true)
    @ApiOperation(value = "Find state by code", nickname = "findByCode", response = State.class, tags={"state"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get A State by code", response = State.class),
    })
    Optional<State> findByCode(@ApiParam(required = true, name = "code", value="Code") @NonNull String code);
}
