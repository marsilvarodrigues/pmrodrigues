package com.pmrodrigues.users.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.enums.AddressType;
import lombok.Builder;

import java.util.Optional;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AddressDTO(
        AddressType addressType,
        String address1,
        String address2,
        String zipcode,
        String neighbor,
        String city,
        StateDTO state,
        UserDTO owner) {

    public static AddressDTO fromAddress(Address address) {
        return new AddressDTO(
                address.getAddressType(),
                address.getAddress1(),
                address.getAddress2(),
                address.getZipcode(),
                address.getNeighbor(),
                address.getCity(),
                StateDTO.fromState(address.getState()),
                UserDTO.fromUser(address.getOwner())
        );
    }

    public Address toAddress() {

            return Address.builder()
                    .address1(address1)
                    .addressType(addressType)
                    .state(Optional.ofNullable(state)
                            .map(StateDTO::toState)
                            .orElse(null))
                    .city(city)
                    .zipcode(zipcode)
                    .address2(address2)
                    .neighbor(neighbor)
                    .owner(Optional.ofNullable(owner)
                            .map(UserDTO::toUser)
                            .orElse(null))
                    .build();

    }
}