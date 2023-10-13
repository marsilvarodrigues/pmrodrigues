package com.pmrodrigues.users.service;

import com.pmrodrigues.commons.exceptions.NotCreateException;
import com.pmrodrigues.commons.exceptions.NotFoundException;
import com.pmrodrigues.commons.exceptions.OperationNotAllowedException;
import com.pmrodrigues.commons.services.DataService;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.clients.EmailClient;
import com.pmrodrigues.users.dtos.UserDTO;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.KeycloakUserRepository;
import com.pmrodrigues.users.repositories.UserRepository;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.pmrodrigues.users.specifications.SpecificationUser.*;
import static java.text.MessageFormat.format;
import static org.springframework.data.jpa.domain.Specification.where;

@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
@Component
public class UserService implements DataService<UUID, UserDTO> {
    public static final String NEW_USER_TEMPLATE = "newUser";
    private final UserRepository repository;
    private final EmailClient emailService;
    private final KeycloakUserRepository keycloakUserRepository;

    @Timed(histogram = true, value = "UserService.getById")
    public User getById(@NonNull final UUID userId) {
        log.info("get user by id {}", userId);
        return repository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    @Timed(histogram = true, value = "UserService.findById")
    public UserDTO findById(@NonNull final UUID userId) {
        log.info("get user by id {}", userId);
        return Optional.of(getById(userId))
                .map(UserDTO::fromUser)
                .orElseThrow(UserNotFoundException::new);
    }

    @Timed(histogram = true, value = "UserService.create")
    @Transactional(propagation = Propagation.REQUIRED)
    @SneakyThrows
    public UserDTO create(@NonNull UserDTO toSave) {

        log.info("creating a new user {}", toSave);

        validateUserDoesNotExist(toSave.email());

        val user = repository.save(toSave.toUser());

        val keycloakId = keycloakUserRepository.insert(user);

        if(!sendWelcomeEmail(user) ){

            keycloakUserRepository.delete(keycloakId);
            throw new NotCreateException();
        }
        user.setExternalId(keycloakId);
        return UserDTO.fromUser(user);

    }


    private boolean sendWelcomeEmail(User user) {
        val email = emailService.getEmailByName(NEW_USER_TEMPLATE)
                .getBody()
                .to(user.getEmail())
                .subject(format("You are Welcome {0} ", user.getFullName()))
                .set("fullName", format("{0} ", user.getFullName()));

        val response = emailService.send(email);
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        } else {
            log.error("Error to create a new user - HTTP Status {} - Reason {}", response.getStatusCode(), response.getBody());
            return false;
        }
    }

    private void validateUserDoesNotExist(String email) {
        val existed = repository.findByEmail(email);
        val existedInKeyCloak = keycloakUserRepository.getUserIdByEmail(email);

        if( existed.isPresent() || !existedInKeyCloak.isEmpty() ) {
            throw new DuplicateKeyException("I´m sorry but this was used before");
        }
    }

    @Timed(histogram = true, value = "UserService.delete")
    @Transactional(propagation = Propagation.REQUIRED)
    @SneakyThrows
    public void delete(@NonNull final UUID id){
        log.info("delete user {} from database", id);
        val user = repository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        keycloakUserRepository.delete(user.getExternalId());
        repository.delete(user);

    }

    @Timed(histogram = true, value = "UserService.findAll")
    @SneakyThrows
    public Page<UserDTO> findAll(@NonNull UserDTO user, @NonNull PageRequest pageRequest){
        log.info("list all users by sample {}", user);
        return repository.findAll(
                    where(firstName(user.firstName())).
                    and(lastName(user.lastName())).
                    and(email(user.email())),
                pageRequest)
                .map(UserDTO::fromUser);
    }

    @Timed(histogram = true, value = "UserService.update")
    @Transactional(propagation = Propagation.REQUIRED)
    public void update(@NonNull final UUID id, @NonNull final UserDTO user) {
        log.info("trying to update user {}",user);
        val existed = repository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        existed.setEmail(user.email());
        existed.setFirstName(user.firstName());
        existed.setLastName(user.lastName());

        repository.save(existed);
        log.info("user {} updated in database",user);

        keycloakUserRepository.update(existed);
        log.info("user {} updated in keycloack",user);
    }

    @Timed(histogram = true, value = "UserService.getAuthenticatedUser")
    public Optional<User> getAuthenticatedUser() {

        val id = SecurityUtils.getUserLoggedId()
                .orElseThrow(NotFoundException::new);
        return repository.findByExternalId(id);
    }


    @Timed(histogram = true, value = "UserService.changePassword")
    public void changePassword(@NonNull final UUID id, @NonNull final String password) {

        val user = repository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        val userLoggedId = SecurityUtils.getUserLoggedId()
                .orElseThrow(NotFoundException::new);

        log.info("changing the password for the user {}", user);

        if( user.getId().equals(userLoggedId) || SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)) {
            user.setPassword(password);
            keycloakUserRepository.changePassword(user);
        } else {
            throw new OperationNotAllowedException();
        }

    }
}
