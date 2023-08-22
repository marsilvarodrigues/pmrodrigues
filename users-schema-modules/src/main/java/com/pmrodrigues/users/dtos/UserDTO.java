package com.pmrodrigues.users.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pmrodrigues.users.model.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record UserDTO(

        UUID id,
        String firstName,
        String lastName,
        String email,
        LocalDateTime expiredDate
){
    public static UserDTO fromUser(User user) {

        return new UserDTO(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(), user.getExpiredDate());

    }

    public User toUser(){
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .expiredDate(expiredDate)
                .build();
    }
}
