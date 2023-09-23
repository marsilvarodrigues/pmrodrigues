package com.pmrodrigues.users.service;


import com.pmrodrigues.security.exceptions.OperationNotAllowedException;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.security.utils.SecurityUtils;
import com.pmrodrigues.users.dtos.PhoneDTO;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.model.Phone;
import com.pmrodrigues.users.repositories.PhoneRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
@Component
public class PhoneService {

    private final UserService userService;
    private final PhoneRepository phoneRepository;



    @Transactional(propagation = Propagation.REQUIRED)
    public Phone createNewPhone(@NonNull PhoneDTO phoneDTO) {
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
}
