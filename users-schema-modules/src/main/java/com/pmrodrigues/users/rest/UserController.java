package com.pmrodrigues.users.rest;

import com.pmrodrigues.commons.request.validates.ValuesAllowed;
import com.pmrodrigues.users.dtos.UserDTO;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;

import static com.pmrodrigues.commons.utils.SortUtils.createSortForString;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/users")
@Validated
public class UserController{

    private final UserService userService;

    @Timed(value = "UserController.add", histogram = true)
    @ApiOperation(value = "Create a new user", nickname = "add", response = User.class, tags={ "user"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User successful created", response = User.class),
            @ApiResponse(code = 400, message = "Failed to create a user"),
            @ApiResponse(code = 409, message = "Other user with the same email exist previously")})
    @PostMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<User> add(@ApiParam(required = true) @Valid @RequestBody final User user) {
        log.info("try to save a new user as {}",user);
        val saved = userService.createNewUser(user);
        log.info("user {} saved into database",saved);

        return ResponseEntity.created(URI.create("/users/" + saved.getId()))
                .body(saved);
    }

    @Timed(value = "UserController.update", histogram = true)
    @ApiOperation(value = "Update a existed user", nickname = "update", tags={"user"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User successful updated"),
            @ApiResponse(code = 404, message = "User not found")})
    @PutMapping(value="/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<String> update(@ApiParam(required = true) @PathVariable("id") final UUID id,@ApiParam(required = true) @Valid @RequestBody final User user){
        log.info("try to update a user {}", user);
        userService.updateUser(id, user);
        log.info("user {} saved", user);
        return ResponseEntity.ok().build();
    }


    @Timed(value = "UserController.getUserById", histogram = true)
    @ApiOperation(value = "Get a user by a specific id", nickname = "getUserById", notes = "Get a user by a specific id", response = User.class, tags={ "user", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class),
            @ApiResponse(code = 404, message = "User not found") })
    @GetMapping(
            value = "/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<User> getUserById(@ApiParam(required = true) @PathVariable("id") final UUID id) {
        log.info("finding user with id {}", id);
        val user = userService.findById(id);
        return ResponseEntity.ok(user);

    }

    @Timed(value = "UserController.listAll", histogram = true)
    @ApiOperation(value = "List all user by", nickname = "listAll", notes = "List all user by", response = User.class, tags={ "user", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class)})
    @GetMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<Page<User>> listAll(
            @RequestParam(name = "page", defaultValue = "0", required = false)
                @Min(value = 0, message = "page number is invalid") @Valid Integer page,
            @RequestParam(name = "size", defaultValue = "50", required = false)
                @Min(value = 1 , message = "page size is invalid")
                @Max(value = 100, message = "page size is bigger than expected")
                @Valid Integer size,
            @RequestParam(name = "sort", defaultValue = "id|desc", required = false)
            @ValuesAllowed(propName = "sort", message = "sort parameter is invalid") @Valid String[] sort,
            @RequestBody(required = false) final UserDTO user){
        log.info("List all users by {}", user);

        var sortBy = createSortForString(sort);

        var sample = new User();
        if( user != null ) {
            sample = user.toUser();
        }

        val response = userService.findAll(sample, PageRequest.of(page, size, Sort.by(sortBy)));
        return ResponseEntity.ok(response);

    }

    @Timed(value = "UserController.deleteById", histogram = true)
    @ApiOperation(value = "Delete User By Id", nickname = "deleteById", notes = "Delete a user by a specific id", response = User.class, tags={ "user", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "User not found") })
    @DeleteMapping(
            value = "/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<String> deleteById(@ApiParam(required = true) @PathVariable("id") final UUID id) {
        log.info("deleting user with id {}", id);
        val user = userService.findById(id);
        userService.delete(user);

        return ResponseEntity.ok()
                .build();
    }
}
