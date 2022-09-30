package com.pmrodrigues.users.keycloak;

import com.pmrodrigues.users.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserFactory {
    public static UserRepresentation createUser(final User user) {
        val userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setUsername(user.getEmail());
        userRepresentation.setEnabled(true);

        val credential = new CredentialRepresentation();
        credential.setTemporary(true);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(user.getPassword());
        userRepresentation.setCredentials(List.of(credential));

        return userRepresentation;
    }
}
