package com.pmrodrigues.users.specifications;

import com.pmrodrigues.users.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationUser {

    public static Specification<User> firstName(String firstName) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%");
    }

    public static Specification<User> lastName(String lastName) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(root.get("lastName"), "%" + lastName + "%");
    }

    public static Specification<User> email(String email) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(root.get("email"), "%" + email + "%");
    }

    public static Specification<User> expiredDate(LocalDateTime expiredDate){
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("expiredDate"), expiredDate);
    }
}
