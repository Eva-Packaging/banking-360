package com.bank.userservice.service;

import com.bank.userservice.dto.RegisterCustomerRequest;
import com.bank.userservice.dto.RegisterCustomerResponse;
import com.bank.userservice.entity.Role;
import com.bank.userservice.entity.User;
import com.bank.userservice.exception.EmailAlreadyExistsException;
import com.bank.userservice.exception.ServerError;
import com.bank.userservice.repository.RoleRepository;
import com.bank.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final Logger logger = LogManager.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterCustomerResponse register(RegisterCustomerRequest customer) {
        User newUser = new User();
        RegisterCustomerResponse returnUser = new RegisterCustomerResponse();

        //checks to see if the role already exists and if it doesnt makes it
        Role role = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> {
                    Role roll = new Role();
                    roll.setName("CUSTOMER");
                    return roleRepository.saveAndFlush(roll);
                });

        try {
            //makes sure your using a unique email
            userRepository.findByEmail(customer.getEmail()).ifPresent(user -> {
                throw new EmailAlreadyExistsException(
                        "Email already in use: " + customer.getEmail()
                );
            });
            //takes customer and make a real user with them
            returnUser.setUserId(newUser.getId());
            returnUser.setStatus(newUser.getStatus());
            returnUser.setCreatedAt(newUser.getCreatedAt());
            returnUser.setFirstName(customer.getFirstName());
            returnUser.setLastName(customer.getLastName());
            returnUser.setEmail(customer.getEmail());
            returnUser.setRoles(Set.of(role.getName()));

            newUser.setFirstName(customer.getFirstName());
            newUser.setLastName(customer.getLastName());
            newUser.setEmail(customer.getEmail());
            newUser.setPhoneNumber(customer.getPhoneNumber());
            //converts a password into a hashcode as well
            newUser.setPasswordHash(passwordEncoder.encode(customer.getPassword()));
            newUser.addRole(role);
//            try {
            userRepository.saveAndFlush(newUser);
//            } catch (Exception e) {
//                log.error("FAILED TO SAVE USER", e);
//                throw e;
//            }

            return returnUser;
        } catch (DataAccessException e) {
            logger.error(e.getMessage());
            throw new ServerError(e.getMessage());
        }
    }
}
