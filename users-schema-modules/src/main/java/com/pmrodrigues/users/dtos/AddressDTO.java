package com.pmrodrigues.users.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.enums.AddressType;
import lombok.Builder;

import java.util.Optional;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AddressDTO(
        UUID id,
        AddressType addressType,
        String address1,
        String address2,
        String zipcode,
        String neighbor,
        String city,
        String state,
        UserDTO owner) {


    public static AddressDTO fromAddress(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getAddressType(),
                address.getAddress1(),
                address.getAddress2(),
                address.getZipcode(),
                address.getNeighbor(),
                address.getCity(),
                address.getState().getCode(),
                UserDTO.fromUser(address.getOwner())
        );
    }

    public Address toAddress() {

            return Address.builder()
                    .address1(address1)
                    .addressType(addressType)
                    .state(State.builder().code(state).build())
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