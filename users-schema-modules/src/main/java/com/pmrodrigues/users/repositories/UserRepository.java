package com.pmrodrigues.users.repositories;

import com.pmrodrigues.users.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, UUID> {
    @Query("SELECT u FROM User u WHERE email = :email")
    Optional<User> findByEmail(@Param("email") String email);

}
