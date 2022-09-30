package com.pmrodrigues.users.service;

import com.pmrodrigues.commons.exceptions.NotCreateException;
import com.pmrodrigues.users.clients.EmailClient;
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

import java.util.UUID;

import static com.pmrodrigues.users.specifications.SpecificationUser.*;
import static java.text.MessageFormat.format;
import static org.springframework.data.jpa.domain.Specification.where;

@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
@Component
public class UserService {
    private final UserRepository repository;
    private final EmailClient emailService;
    private final KeycloakUserRepository keycloakUserRepository;

    @Timed(histogram = true, value = "UserService.findById")
    public User findById(@NonNull final UUID userId) {
        log.info("get user by id {}", userId);
        return repository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    @Timed(histogram = true, value = "UserService.createNewUser")
    @Transactional(propagation = Propagation.REQUIRED)
    @SneakyThrows
    public User createNewUser(@NonNull User user) {

        log.info("creating a new user {}", user);

        val existed = repository.findByEmail(user.getEmail());
        val existedInKeyCloak = keycloakUserRepository.getUserIdByEmail(user.getEmail());

        if( existed.isPresent() || !existedInKeyCloak.isEmpty() )
            throw new DuplicateKeyException("I´m sorry but this was used before");

        user = repository.save(user);

        val keycloakId = keycloakUserRepository.insert(user);

        val email = emailService.getEmailByName("newUser")
                        .getBody()
                        .to(user.getEmail())
                        .subject(format("You are Welcome {0} ", user.getFullName()))
                        .set("fullName",format("{0} ", user.getFullName()));

        val response = emailService.send(email);
        if( response.getStatusCode() == HttpStatus.OK ) {
            user.setExternalId(keycloakId);
            return user;
        }else{
            log.error("Error to create a new user - HTTP Status {} - Reason {}", response.getStatusCode(), response.getBody());
            keycloakUserRepository.delete(keycloakId);
            throw new NotCreateException();
        }
    }

    @Timed(histogram = true, value = "UserService.delete")
    @Transactional(propagation = Propagation.REQUIRED)
    @SneakyThrows
    public void delete(@NonNull final User user){
        log.info("delete user {} from database", user);
        val toDelete = repository.findById(user.getId())
                .orElseThrow(UserNotFoundException::new);

        repository.delete(toDelete);
        keycloakUserRepository.delete(toDelete.getId());
        log.info("user {} deleted from database", user);

    }

    @Timed(histogram = true, value = "UserService.findAll")
    @SneakyThrows
    public Page<User> findAll(@NonNull User user, @NonNull PageRequest pageRequest){
        log.info("list all users by {}", user);
        return repository.findAll(
                    where(firstName(user.getFirstName())).
                    and(lastName(user.getLastName())).
                    and(email(user.getEmail())).
                    and(expiredDate(user.getExpiredDate())),
                pageRequest);
    }

    @Timed(histogram = true, value = "UserService.updateUser")
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUser(@NonNull final UUID id, @NonNull final User user) {
        log.info("trying to update user {}",user);
        val existed = repository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        existed.setEmail(user.getEmail());
        existed.setFirstName(user.getFirstName());
        existed.setLastName(user.getLastName());

        repository.save(existed);
        log.info("user {} updated in database",user);

        keycloakUserRepository.update(existed);
        log.info("user {} updated in keycloack",user);


    }
}
