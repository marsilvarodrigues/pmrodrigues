package com.pmrodrigues.users.specifications;

import com.pmrodrigues.users.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationUser {

    public static Specification<User> firstName(String firstName) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if(!isBlank(firstName) && !isEmpty(firstName))
                return criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%");
            else
                return null;
        };
    }

    public static Specification<User> lastName(String lastName) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if(!isBlank(lastName) && !isEmpty(lastName))
                return criteriaBuilder.like(root.get("lastName"), "%" + lastName + "%");
            else
                return null;
        };

    }

    public static Specification<User> email(String email) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if(!isBlank(email) && !isEmpty(email))
                return criteriaBuilder.like(root.get("email"), "%" + email + "%");
            else
                return null;
        };

    }

    public static Specification<User> externalId(List<UUID> externalIds) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( externalIds != null)
                return criteriaBuilder.in(root.get("externalId")).value(externalIds);
            else
                return null;
        };
    }
}
