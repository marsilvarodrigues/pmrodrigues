package com.pmrodrigues.users.keycloak;

import com.pmrodrigues.users.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserFactory {
    public static UserRepresentation createUser(final User user) {
        val userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEmail(user.getEmail());
        if( user.isNew() ) {
            userRepresentation.setEmailVerified(true);
            userRepresentation.setUsername(user.getEmail());
            userRepresentation.setEnabled(true);
        }

        if( !isBlank(user.getPassword()) ) {
            val credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(user.getPassword());
            userRepresentation.setCredentials(List.of(credential));
        }

        return userRepresentation;
    }
}
