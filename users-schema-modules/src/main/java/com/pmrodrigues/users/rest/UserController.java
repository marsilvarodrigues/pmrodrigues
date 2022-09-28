package com.pmrodrigues.users.rest;

import com.pmrodrigues.commons.request.validates.ValuesAllowed;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/users")
@Validated
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
    public ResponseEntity<Page<User>> listAll(
            @RequestParam(name = "page", defaultValue = "0", required = false)
                @Min(value = 0, message = "page number is invalid") @Valid Integer page,
            @RequestParam(name = "size", defaultValue = "50", required = false)
                @Min(value = 1 , message = "page size is invalid")
                @Max(value = 100, message = "page size is bigger than expected")
                @Valid Integer size,
            @RequestParam(name = "sort", defaultValue = "id.desc", required = false)
            @ValuesAllowed(propName = "sort", message = "sort parameter is invalid") @Valid String[] sort){
        log.info("List all users");

        var sortBy = Stream.of(sort)
                    .map(s -> {
                        var sortRule = s.split("\\.");
                        if( sortRule.length == 1) return Sort.Order.asc(sortRule[0]);
                        if(Sort.Direction.ASC == Sort.Direction.valueOf(sortRule[1].toUpperCase())){
                            return Sort.Order.asc(sortRule[0]);
                        }else{
                            return  Sort.Order.desc(sortRule[0]);
                        }
                    })
                    .toList();

        val user = userService.findAll(PageRequest.of(page, size, Sort.by(sortBy)));
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
