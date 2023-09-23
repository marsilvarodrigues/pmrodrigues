package com.pmrodrigues.users.dtos;

import com.pmrodrigues.users.model.Phone;
import com.pmrodrigues.users.model.enums.PhoneType;

import java.util.Optional;
import java.util.UUID;

public record PhoneDTO(
        UUID id,
        UserDTO owner,
        String phoneNumber,
        PhoneType type
) {
    public static PhoneDTO fromPhone(Phone phone) {
        return new PhoneDTO(
                phone.getId(),
                UserDTO.fromUser(phone.getOwner()),
                phone.getPhoneNumber(),
                phone.getType());
    }

    public Phone toPhone() {

        return Phone.builder()
                .phoneNumber(phoneNumber)
                .type(type)
                .owner(Optional.ofNullable(owner)
                        .map(UserDTO::toUser)
                        .orElse(null))
                .id(id)
                .build();

    }
}
