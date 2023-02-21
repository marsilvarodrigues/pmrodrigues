package com.pmrodrigues.users.dtos;

import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDTO {


    private AddressType addressType;
    private String address1;

    private String zipcode;

    private String neightboor;

    private String city;

    private State state;

    private User owner;

    public Address toAddress() {
        return Address.builder()
                .address1(address1)
                .addressType(addressType)
                .zipcode(zipcode)
                .neightboor(neightboor)
                .city(city)
                .state(state)
                .owner(owner)
                .build();
    }
}
