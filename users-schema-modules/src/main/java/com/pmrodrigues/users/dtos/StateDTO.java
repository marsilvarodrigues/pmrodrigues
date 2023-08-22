package com.pmrodrigues.users.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pmrodrigues.users.model.State;
import lombok.Builder;

import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record StateDTO(
        UUID id,
        String name,
        String code) {

    public static StateDTO fromState(State state) {
        return new StateDTO(state.getId(), state.getName(), state.getCode());
    }

    public State toState() {
        return State
                .builder()
                .id(id)
                .name(name)
                .code(code)
                .build();
    }
}
