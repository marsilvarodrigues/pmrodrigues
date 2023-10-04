package com.pmrodrigues.users.service;


import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.dtos.PhoneDTO;
import com.pmrodrigues.users.exceptions.PhoneNotFoundException;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.Phone;
import com.pmrodrigues.users.repositories.PhoneRepository;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.pmrodrigues.users.specifications.SpecificationPhone.owner;
import static com.pmrodrigues.users.specifications.SpecificationPhone.type;

@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
@Component
public class PhoneService {

    private final UserService userService;
    private final PhoneRepository phoneRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    @Timed(histogram = true, value = "PhoneService.createNewPhone")
    public Phone create(@NonNull PhoneDTO phoneDTO) {
        log.info("adding a new phone {}", phoneDTO);

        var phone = phoneDTO.toPhone();

        val loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);

        var user = loggedUser;

        if( phoneDTO.owner() != null )
            user = userService.findById(phoneDTO.owner().id());

        if( user.equals(loggedUser) || SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ){
            phone = phone.withOwner(user);
            return phoneRepository.save(phone);
        } else {
            throw new OperationNotAllowedException();
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Timed(histogram = true, value = "PhoneService.updatePhone")
    public void update(@NonNull UUID id, @NonNull PhoneDTO phoneDTO) {
        log.info("updating phone {} by id {}", phoneDTO, id);

        var phone = phoneRepository.findById(id).orElseThrow(PhoneNotFoundException::new);
        val loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);

        val owner = userService.findById(phoneDTO.owner().id());
        if( loggedUser.equals(owner) || SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ){
            phone.withPhoneNumber(phoneDTO.phoneNumber())
                    .withOwner(owner)
                    .withType(phoneDTO.type());

            phoneRepository.save(phone);
        } else {
            throw new OperationNotAllowedException();
        }


    }

    @Timed(histogram = true, value = "PhoneService.findAll")
    public Page<Phone> findAll(@NonNull PhoneDTO phone, @NonNull PageRequest pageRequest) {
        log.info("list all phones by sample {}", phone);
        var loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);

        if( SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ) {
            return phoneRepository.findAll(owner(phone.owner().toUser())
                    .and(type(phone.type())), pageRequest);
        } else {
            return phoneRepository.findAll(owner(loggedUser)
                    .and(type(phone.type())), pageRequest);
        }
    }

    @Timed(histogram = true, value = "PhoneService.findById")
    public Phone findById(@NonNull UUID id) {
        log.info("searching phone by id {}", id);
        var loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);
        val phone =  phoneRepository.findById(id)
                .orElseThrow(PhoneNotFoundException::new);
        if(SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN)) {
            return phone;
        }else{
            if( !phone.getOwner().equals(loggedUser) ) {
                throw new PhoneNotFoundException();
            }
        }
        return phone;
    }
    @Timed(histogram = true, value = "PhoneService.findById")
    public void delete(@NonNull UUID id) {
        log.info("deleting phone {}", id);
        var loggedUser = userService.getAuthenticatedUser()
                .orElseThrow(UserNotFoundException::new);
        val phone = this.findById(id);

        if( SecurityUtils.isUserInRole(Security.SYSTEM_ADMIN) ){
            phoneRepository.delete(phone);
        } else {
            if( phone.getOwner().equals(loggedUser) ) {
                phoneRepository.delete(phone);
            } else {
                throw new PhoneNotFoundException();
            }
        }
    }
}
