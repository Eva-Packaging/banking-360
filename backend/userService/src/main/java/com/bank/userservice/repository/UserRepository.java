package com.bank.userservice.repository;

import com.bank.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    //returns nothing or the user with the given email
    Optional<User> findByEmail(String email);
    //returns nothing or the user with the given first and last name
    Optional<User> findByFirstNameAndLastName(String firstName, String lastName);
}
