package com.pmrodrigues.users.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RoleDTO (
        String id,
        String name
){

}
