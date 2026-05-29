package com.bank.userservice.service;

import com.bank.userservice.dto.RegisterCustomerRequest;
import com.bank.userservice.entity.Role;
import com.bank.userservice.entity.User;
import com.bank.userservice.exception.EmailAlreadyExistsException;
import com.bank.userservice.repository.RoleRepository;
import com.bank.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterCustomerRequest customer) {
        User newUser = new User();

        //checks to see if the role already exists and if it doesnt makes it
        Role role = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> {
                    Role roll = new Role();
                    roll.setName("CUSTOMER");
                    return roleRepository.saveAndFlush(roll);
                });
        //makes sure your using a unique email
        userRepository.findByEmail(customer.getEmail()).ifPresent(user -> {
                    throw new EmailAlreadyExistsException(
                            "Email already in use: " + customer.getEmail()
                    );
                });
        //takes customer and make a real user with them
        newUser.setFirstName(customer.getFirstName());
        newUser.setLastName(customer.getLastName());
        newUser.setEmail(customer.getEmail());
        newUser.setPhoneNumber(customer.getPhoneNumber());
        //converts a password into a hashcode as well
        newUser.setPasswordHash(passwordEncoder.encode(customer.getPassword()));
        newUser.addRole(role);
        try {
            userRepository.saveAndFlush(newUser);
        } catch (Exception e) {
            log.error("FAILED TO SAVE USER", e);
            throw e;
        }
    }
}
