package com.pmrodrigues.users.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pmrodrigues.users.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserDTO implements Serializable {

    private String firstName;

    private String lastName;

    private String email;

    private LocalDateTime expiredDate;

    public User toUser(){
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .expiredDate(expiredDate)
                .build();
    }
}
