package com.pmrodrigues.users.rest;

import com.pmrodrigues.commons.request.validates.ValuesAllowed;
import com.pmrodrigues.commons.rest.DataController;
import com.pmrodrigues.users.dtos.PhoneDTO;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.service.PhoneService;
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
import java.util.Optional;
import java.util.UUID;

import static com.pmrodrigues.commons.data.utils.SortUtils.createSortForString;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/phones")
@Validated
public class PhoneController implements DataController<UUID, PhoneDTO> {

    private final PhoneService phoneService;

    @Timed(value = "PhoneController.getById", histogram = true)
    @ApiOperation(value = "Get a phone by a specific id", nickname = "getById", notes = "Get a phone by a specific id", response = PhoneDTO.class, tags={ "phone", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Address.class),
            @ApiResponse(code = 404, message = "Phone not found") })
    @GetMapping(
            value = "/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<PhoneDTO> getById(@PathVariable("id") UUID id) {
        log.info("get phone by id {}", id);
        val phone = phoneService.findById(id);

        return ResponseEntity.ok(phone);
    }

    @Timed(value = "PhoneController.add", histogram = true)
    @ApiOperation(value = "Create a new phone", nickname = "add", response = PhoneDTO.class, tags={ "phone"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Address successful created", response = PhoneDTO.class),
            @ApiResponse(code = 403, message = "User not allowed to do this operation"),
            @ApiResponse(code = 400, message = "Failed to create a address")})
    @PostMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<PhoneDTO> add(@ApiParam(required = true) @Valid @RequestBody final PhoneDTO phone) {
        log.info("try to save a new phone as {}",phone);
        val saved = phoneService.create(phone);
        log.info("phone {} saved into database",saved);

        return ResponseEntity.created(URI.create("/phones/" + saved.id()))
                .body(saved);
    }

    @Timed(value = "PhoneController.update", histogram = true)
    @ApiOperation(value = "Update a existed phone", nickname = "update", tags={"phone"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Phone successful updated"),
            @ApiResponse(code = 404, message = "Phone not found"),
            @ApiResponse(code = 403, message = "User not allowed to do this operation")})
    @PutMapping(value="/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<String> update(@ApiParam(required = true) @PathVariable("id") final UUID id,@ApiParam(required = true) @Valid @RequestBody final PhoneDTO phone){
        log.info("try to update a phone {}", phone);
        phoneService.update(id, phone);
        log.info("phone {} saved", phone);
        return ResponseEntity.ok().build();
    }

    @Timed(value = "PhoneController.deleteById", histogram = true)
    @ApiOperation(value = "Delete phone By Id", nickname = "deleteById", notes = "Delete a phone by a specific id", tags={ "phone", })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 404, message = "Address not found") })
    @DeleteMapping(
            value = "/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<String> deleteById(@ApiParam(required = true) @PathVariable("id") final UUID id) {
        log.info("deleting user with id {}", id);
        phoneService.delete(id);

        return ResponseEntity.noContent()
                .build();
    }

    @Timed(value = "PhoneController.listAll", histogram = true)
    @ApiOperation(value = "List all phones by", nickname = "listAll",
            notes = "List all phones by",
            response = PhoneDTO.class, tags={ "phone", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PhoneDTO.class)})
    @GetMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<Page<PhoneDTO>> listAll(
            @RequestParam(name = "page", defaultValue = "0", required = false)
            @Min(value = 0, message = "page number is invalid") @Valid Integer page,
            @RequestParam(name = "size", defaultValue = "50", required = false)
            @Min(value = 1 , message = "page size is invalid")
            @Max(value = 100, message = "page size is bigger than expected")
            @Valid Integer size,
            @RequestParam(name = "sort", defaultValue = "id|desc", required = false)
            @ValuesAllowed(propName = "sort", message = "sort parameter is invalid") @Valid String[] sort,
            @RequestBody(required = false) final PhoneDTO phone){

        log.info("search all phones based on page {}, size {}, sort {}, sample {}", page, size, sort, phone );

        var sortBy = createSortForString(sort);

        var sample = Optional.ofNullable(phone)
                .orElse(new PhoneDTO(null, null, null, null));

        val response = phoneService.findAll(sample , PageRequest.of(page, size, Sort.by(sortBy)));
        return ResponseEntity.ok(response);

    }
}
