package com.pmrodrigues.users.specifications;

import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import static org.apache.commons.lang3.StringUtils.isBlank;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationAddress {

    public static Specification<Address> state(State state) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( state != null )
                return criteriaBuilder.equal(root.get("state"), state);
            else
                return null;
        };

    }

    public static Specification<Address> city(String city){
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( !isBlank(city) )
                return criteriaBuilder.like(root.get("city"), city + "%");
            else return null;
        };


    }

    public static Specification<Address> neightboor(String neightboor){
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( !isBlank(neightboor) )
                return criteriaBuilder.like(root.get("neightboor"), neightboor + "%");
            else return null;
        };

    }

    public static Specification<Address> address(String address){
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( !isBlank(address) )
                return criteriaBuilder.like(root.get("address1"), address + "%");
            else return null;
        };

    }

    public static Specification<Address> zipcode(String zipcode){
        return (root, criteriaQuery, criteriaBuilder) -> {
            if( !isBlank(zipcode) )
                return criteriaBuilder.like(root.get("zipcode"), zipcode + "%");
            else return null;
        };

    }

    public static Specification<Address> owner(User owner) {

        return (root, criteriaQuery, criteriaBuilder) -> {
            if( owner != null )
                return criteriaBuilder.equal(root.get("owner"), owner);
            else return null;
        };

    }

}
