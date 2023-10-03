package com.pmrodrigues.users.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pmrodrigues.users.model.User;
import lombok.Builder;

import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record UserDTO(

        UUID id,
        String firstName,
        String lastName,
        String email
){
    public static UserDTO fromUser(User user) {

        return new UserDTO(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());

    }

    public User toUser(){
        return User.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();
    }
}
