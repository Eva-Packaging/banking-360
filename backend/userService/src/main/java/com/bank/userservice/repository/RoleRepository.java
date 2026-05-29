package com.bank.userservice.repository;

import com.bank.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    // returns either nothing or every role somone of the given name has
    Optional<Role> findByName(String name);
}
