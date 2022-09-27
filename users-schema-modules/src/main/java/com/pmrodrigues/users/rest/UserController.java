package com.pmrodrigues.users.rest;

import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.service.UserService;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class UserController{

    private final UserService userService;

    @Timed(histogram = true)
    @ApiOperation(value = "Create a new user", nickname = "add", response = User.class, tags={ "user"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User successful created", response = User.class),
            @ApiResponse(code = 400, message = "Failed to create a user"),
            @ApiResponse(code = 409, message = "Other user with the same email exist previously")})
    @RequestMapping(
            method = RequestMethod.POST,
            produces = { "application/json" },
            consumes = { "application/json" }
    )
    public ResponseEntity<User> add(@ApiParam(required = true) @Valid @RequestBody final User user) {
        log.info("try to save a new user as {}",user);
        val saved = userService.createNewUser(user);
        log.info("user {} saved into database",saved);

        return ResponseEntity.created(URI.create("/users/" + saved.getId()))
                .body(saved);
    }

    @Timed(histogram = true)
    @ApiOperation(value = "Update a existed user", nickname = "update", tags={"user"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User successful updated"),
            @ApiResponse(code = 404, message = "User not found")})
    @RequestMapping(value="/{id}",
            method = RequestMethod.PUT,
            produces = { "application/json" },
            consumes = { "application/json" }
    )
    public ResponseEntity update(@ApiParam(required = true) @PathVariable("id") final UUID id,@ApiParam(required = true) @Valid @RequestBody final User user){
        log.info("try to update a user {}", user);
        userService.updateUser(id, user);
        log.info("user {} saved", user);
        return ResponseEntity.ok().build();
    }


    @Timed(histogram = true)
    @ApiOperation(value = "Get a user by a specific id", nickname = "getUserById", notes = "Get a user by a specific id", response = User.class, tags={ "user", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class),
            @ApiResponse(code = 404, message = "User not found") })
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}",
            produces = { "application/json" }
    )
    public ResponseEntity<User> getUserById(@ApiParam(required = true) @PathVariable("id") final UUID id) {
        log.info("finding user with id {}", id);
        val user = userService.findById(id);
        return ResponseEntity.ok(user);

    }

    @Timed(histogram = true)
    @ApiOperation(value = "List all user by", nickname = "listAll", notes = "List all user by", response = User.class, tags={ "user", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class)})
    @RequestMapping(
            method = RequestMethod.GET,
            produces = { "application/json" }
    )
    public ResponseEntity<Iterable<User>> listAll() {
        log.info("List all users");
        val user = userService.findAll();
        return ResponseEntity.ok(user);

    }

    @Timed(histogram = true)
    @ApiOperation(value = "Delete User By Id", nickname = "deleteById", notes = "Delete a user by a specific id", response = User.class, tags={ "user", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "User not found") })
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/{id}",
            produces = { "application/json" }
    )
    public ResponseEntity deleteById(@ApiParam(required = true) @PathVariable("id") final UUID id) {
        log.info("deleting user with id {}", id);
        val user = userService.findById(id);
        userService.delete(user);

        return ResponseEntity.ok()
                .build();
    }
}
