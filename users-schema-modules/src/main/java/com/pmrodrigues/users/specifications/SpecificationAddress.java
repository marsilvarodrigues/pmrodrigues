package com.pmrodrigues.users.specifications;

import com.pmrodrigues.users.model.Address;
import com.pmrodrigues.users.model.State;
import com.pmrodrigues.users.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationAddress {

    public static Specification<Address> state(State state) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), state);
    }

    public static Specification<Address> city(String city){
        return (root, criteriaQuery, criteriaBuilder) ->
            criteriaBuilder.like(root.get("city"), city + "%");

    }

    public static Specification<Address> neightboor(String neightboor){
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(root.get("neightboor"), neightboor + "%");

    }

    public static Specification<Address> address(String address){
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(root.get("address1"), "%" + address + "%");

    }

    public static Specification<Address> zipcode(String zipcode){
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("zipcode"), zipcode);

    }

    public static Specification<Address> owner(User owner) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("owner"), owner);
    }

}
