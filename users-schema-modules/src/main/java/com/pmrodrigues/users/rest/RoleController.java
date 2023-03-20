package com.pmrodrigues.users.rest;

import com.pmrodrigues.commons.request.validates.ValuesAllowed;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.service.RoleService;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation(value = "List all user by role", nickname = "getUserInRole", notes = "List all user by role", response = User.class, tags={ "user", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class)})
    @GetMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<Page<User>> getUserInRole(@RequestParam(name = "role") @NonNull String role,
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

        val response = roleService.getUserInRole(role, PageRequest.of(page, size, Sort.by(sortBy)));
        return ResponseEntity.ok(response);

    }

    @Timed(value = "RoleController.applyRoleToUser", histogram = true)
    @ApiOperation(value = "Add a role to User", nickname = "applyRoleToUser", notes = "Add a role to User", tags={ "user", })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 404, message = "User (or Role) not found")})
    @PostMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity applyRoleToUser(@RequestParam(name = "role") @NonNull String role, @RequestParam(name = "user") @NonNull UUID id) {
        log.info("applying role {} to user {}", role, id);

        val user = User.builder().externalId(id).build();
        roleService.applyRoleToUser(user, role);

        return ResponseEntity.noContent().build();
    }
}
