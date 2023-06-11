package com.pmrodrigues.users.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.enums.AddressType;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AddressDTO(
        AddressType addressType,
        String address1,
        String address2,
        String zipcode,
        String neightbor,
        String city,
        StateDTO state,
        UserDTO owner) {

    public Address toAddress() {

            return Address.builder()
                    .address1(address1)
                    .addressType(addressType)
                    .state(state.toState())
                    .city(city)
                    .zipcode(zipcode)
                    .address2(address2)
                    .neightbor(neightbor)
                    .owner(owner.toUser())
                    .build();

    }
}



