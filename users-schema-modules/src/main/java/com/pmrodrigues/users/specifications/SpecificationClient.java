package com.pmrodrigues.users.specifications;

import com.pmrodrigues.users.model.Client;
import com.pmrodrigues.users.model.State;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationClient {

    public static Specification<Client> firstName(String firstName) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if(!isBlank(firstName) && !isEmpty(firstName))
                return criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%");
            else
                return null;
        };
    }

    public static Specification<Client> lastName(String lastName) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if(!isBlank(lastName) && !isEmpty(lastName))
                return criteriaBuilder.like(root.get("lastName"), "%" + lastName + "%");
            else
                return null;
        };

    }

    public static Specification<Client> email(String email) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if(!isBlank(email) && !isEmpty(email))
                return criteriaBuilder.like(root.get("email"), "%" + email + "%");
            else
                return null;
        };

    }

    public static Specification<Client> externalId(List<UUID> externalIds) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( externalIds != null)
                return criteriaBuilder.in(root.get("externalId")).value(externalIds);
            else
                return null;
        };
    }

    public static Specification<Client> olderThan(Integer age){
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( age != null ) {
                val currentDate = LocalDate.now();
                val ageExpression = criteriaBuilder.diff(currentDate.getYear(),
                        criteriaBuilder.function("year", Integer.class, root.get("birthday")));

                return criteriaBuilder.greaterThanOrEqualTo(ageExpression, age);
            }else{
                return null;
            }
        };
    }

    public static Specification<Client> state(State state) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( state != null ) {
                val join = root.join("addresses", JoinType.INNER);
                return criteriaBuilder.equal(join.get("state"),state);

            }else return null;
        };
    }

    public static Specification<Client> city(String city) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( !isBlank(city) ) {
                val join = root.join("addresses", JoinType.INNER);
                return criteriaBuilder.like(join.get("city"),"%" + city + "%");

            }else return null;
        };
    }
}
