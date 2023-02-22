package com.pmrodrigues.users.rest;

import com.pmrodrigues.commons.request.validates.ValuesAllowed;
import com.pmrodrigues.users.dtos.AddressDTO;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.service.AddressService;
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
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/addresses")
@Validated
public class AddressController {

    private final AddressService addressService;

    @Timed(histogram = true)
    @ApiOperation(value = "Get a address by a specific id", nickname = "getAddressById", notes = "Get a address by a specific id", response = Address.class, tags={ "address", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class),
            @ApiResponse(code = 404, message = "Address not found") })
    @GetMapping(
            value = "/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<Address> getAddressById(@NonNull @ApiParam(required = true) @PathVariable(name = "id") UUID id) {
        log.info("try to read address by id {}", id);
        val address = addressService.findById(id);
        return ResponseEntity.ok(address);
    }

    @Timed(histogram = true)
    @ApiOperation(value = "List all address by", nickname = "listAll", notes = "List all addresses by", response = Address.class, tags={ "address", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class)})
    @GetMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<Page<Address>> listAll(
            @RequestParam(name = "page", defaultValue = "0", required = false)
            @Min(value = 0, message = "page number is invalid") @Valid Integer page,
            @RequestParam(name = "size", defaultValue = "50", required = false)
            @Min(value = 1 , message = "page size is invalid")
            @Max(value = 100, message = "page size is bigger than expected")
            @Valid Integer size,
            @RequestParam(name = "sort", defaultValue = "id|desc", required = false)
            @ValuesAllowed(propName = "sort", message = "sort parameter is invalid") @Valid String[] sort,
            @RequestBody(required = false) final AddressDTO address){

        log.info("search all addresses based on page {}, size {}, sort {}, sample {}", page, size, sort, address );

        var sortBy = Stream.of(sort)
                .map(s -> {
                    var sortRule = s.split("\\|");
                    if( sortRule.length == 1) return Sort.Order.asc(sortRule[0]);
                    if(Sort.Direction.ASC == Sort.Direction.valueOf(sortRule[1].toUpperCase())){
                        return Sort.Order.asc(sortRule[0]);
                    }else{
                        return  Sort.Order.desc(sortRule[0]);
                    }
                })
                .toList();

        var sample = new Address();
        if( address != null ) sample = address.toAddress();

        val response = addressService.findAll(sample, PageRequest.of(page, size, Sort.by(sortBy)));
        return ResponseEntity.ok(response);

    }

    @Timed(histogram = true)
    @ApiOperation(value = "Create a new address", nickname = "add", response = Address.class, tags={ "address"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Address successful created", response = Address.class),
            @ApiResponse(code = 403, message = "User not allowed to do this operation"),
            @ApiResponse(code = 400, message = "Failed to create a address")})
    @PostMapping(
            produces = { MediaType.APPLICATION_JSON_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<Address> add(@ApiParam(required = true) @Valid @RequestBody final Address address) {
        log.info("try to save a new address as {}",address);
        val saved = addressService.createNewAddress(address);
        log.info("address {} saved into database",saved);

        return ResponseEntity.created(URI.create("/address/" + saved.getId()))
                .body(saved);
    }

    @Timed(histogram = true)
    @ApiOperation(value = "Update a existed address", nickname = "update", tags={"address"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Address successful updated"),
            @ApiResponse(code = 404, message = "Address not found"),
            @ApiResponse(code = 403, message = "User not allowed to do this operation")})
    @PutMapping(value="/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<String> update(@ApiParam(required = true) @PathVariable("id") final UUID id,@ApiParam(required = true) @Valid @RequestBody final Address address){
        log.info("try to update a address {}", address);
        addressService.updateAddress(id, address);
        log.info("address {} saved", address);
        return ResponseEntity.ok().build();
    }

    @Timed(histogram = true)
    @ApiOperation(value = "Delete Address By Id", nickname = "deleteById", notes = "Delete a address by a specific id", response = Address.class, tags={ "address", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Address not found") })
    @DeleteMapping(
            value = "/{id}",
            produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    public ResponseEntity<String> deleteById(@ApiParam(required = true) @PathVariable("id") final UUID id) {
        log.info("deleting user with id {}", id);
        val address = addressService.findById(id);
        addressService.delete(address);

        return ResponseEntity.ok()
                .build();
    }

}