package com.pmrodrigues.users.specifications;

import com.pmrodrigues.users.model.Phone;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.model.enums.PhoneType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationPhone {
    public static Specification<Phone> owner(User owner) {

        return (root, criteriaQuery, criteriaBuilder) -> {
            if( owner != null )
                return criteriaBuilder.equal(root.get("owner"), owner);
            else return null;
        };

    }

    public static Specification<Phone> type(PhoneType type) {

        return (root, criteriaQuery, criteriaBuilder) -> {
            if( type != null )
                return criteriaBuilder.equal(root.get("type"), type);
            else return null;
        };

    }

}
