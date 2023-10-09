package com.pmrodrigues.commons.rest;

import com.pmrodrigues.commons.request.validates.ValuesAllowed;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public interface DataController<I, E>{

    ResponseEntity<E> getById(@NonNull @ApiParam(required = true) @PathVariable(name = "id") I id);

    ResponseEntity<Page<E>> listAll(
            @RequestParam(name = "page", defaultValue = "0", required = false)
            @Min(value = 0, message = "page number is invalid") @Valid Integer page,
            @RequestParam(name = "size", defaultValue = "50", required = false)
            @Min(value = 1 , message = "page size is invalid")
            @Max(value = 100, message = "page size is bigger than expected")
            @Valid Integer size,
            @RequestParam(name = "sort", defaultValue = "id|desc", required = false)
            @ValuesAllowed(propName = "sort", message = "sort parameter is invalid") @Valid String[] sort,
            @RequestBody(required = false) final E sample);

    ResponseEntity<E> add(@ApiParam(required = true) @Valid @RequestBody final E e);

    ResponseEntity<String> update(@ApiParam(required = true) @PathVariable("id") final I id, @ApiParam(required = true) @Valid @RequestBody final E e);


    ResponseEntity<String> deleteById(@ApiParam(required = true) @PathVariable("id") final I id);



}
