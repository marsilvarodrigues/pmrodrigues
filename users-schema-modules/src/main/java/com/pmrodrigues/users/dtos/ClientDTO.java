package com.pmrodrigues.users.dtos;

import com.pmrodrigues.users.model.Client;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ClientDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate,
        List<AddressDTO> addresses,
        List<PhoneDTO> phones
) {

    public static ClientDTO fromClient(Client client) {
        return new ClientDTO(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getBirthday(),
                Optional.ofNullable(client.getAddresses())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(AddressDTO::fromAddress).toList(),
                Optional.ofNullable(client.getPhones())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(PhoneDTO::fromPhone).toList()
        );
    }

    public Client toClient() {
        return Client.builder()
                .id(id())
                .firstName(firstName())
                .lastName(lastName())
                .email(email())
                .birthday(birthDate())
                .phones(Optional.ofNullable(phones())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(PhoneDTO::toPhone).toList())
                .addresses(
                        Optional.ofNullable(addresses())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(AddressDTO::toAddress).toList())
                .build();
    }

}
