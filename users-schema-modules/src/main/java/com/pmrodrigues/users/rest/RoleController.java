package com.pmrodrigues.users.rest;

import com.pmrodrigues.commons.request.validates.ValuesAllowed;
import com.pmrodrigues.users.dtos.RoleDTO;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.service.RoleService;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
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
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.UUID;

import static com.pmrodrigues.commons.data.utils.SortUtils.createSortForString;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/roles")
@Validated
public class RoleController {

    private final RoleService roleService;

    @Timed(value = "RoleController.getUserInRole", histogram = true)
    @ApiOperation(value = "List all user by role", nickname = "getUserInRole", notes = "List all user by role", response = User.class, tags={  "user", "role"  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class)})
    @GetMapping(
            value = "/{role}/users",
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<Page<UserDTO>> getUsersInRole(@PathVariable(name = "role") @ApiParam(required = true) @NonNull String role,
                                                     @RequestParam(name = "page", defaultValue = "0", required = false)
                                                    @PositiveOrZero(message = "Page number must be positive or zero")
                                                    @Min(value = 0, message = "page number is invalid") @Valid Integer page,
                                                     @RequestParam(name = "size", defaultValue = "50", required = false)
                                                        @Positive(message = "Page size must be positive")
                                                        @Min(value = 1 , message = "page size is invalid")
                                                        @Max(value = 100, message = "page size is bigger than expected")
                                                        @Valid Integer size,
                                                     @RequestParam(name = "sort", defaultValue = "id|desc", required = false)
                                                        @ValuesAllowed(propName = "sort", message = "sort parameter is invalid") @Valid String[] sort){
        log.info("List all users in role {}" , role);

        var sortBy = createSortForString(sort);

        val response = roleService.getUsersInRole(role, PageRequest.of(page, size, Sort.by(sortBy))).map(UserDTO::fromUser);
        return ResponseEntity.ok(response);

    }

    @Timed(value = "RoleController.applyRoleToUser", histogram = true)
    @ApiOperation(value = "Add a role to User", nickname = "applyRoleToUser", notes = "Add a role to User", tags={ "user", "role" })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 404, message = "User (or Role) not found")})
    @PostMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<String> applyRoleToUser(@RequestParam(name = "role") @ApiParam(required = true) @NonNull String role,
                                          @RequestParam(name = "user") @ApiParam(required = true) @NonNull UUID id) {
        log.info("applying role {} to user {}", role, id);

        val user = User.builder()
                            .externalId(id)
                            .build();

        roleService.applyRoleToUser(user, role);

        return ResponseEntity.noContent().build();
    }

    @Timed(value = "RoleController.getRoles", histogram = true)
    @ApiOperation(value = "Add a role to User", nickname = "getRoles", notes = "List all Roles", tags={ "role", }, response = RoleDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK")})
    @GetMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<List<RoleDTO>> getRoles() {
        log.info("list all roles");
        return ResponseEntity.ok(roleService.getRoles());
    }
}
