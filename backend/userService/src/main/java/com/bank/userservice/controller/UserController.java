package com.bank.userservice.controller;

import com.bank.userservice.dto.RegisterCustomerResponse;
import com.bank.userservice.entity.Role;
import com.bank.userservice.repository.UserRepository;
import com.bank.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwt;

    @GetMapping("/me")
    public ResponseEntity<RegisterCustomerResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(
                userRepository.findByEmail(jwt.extractEmail(
                        authHeader.replaceFirst("^\\S+\\s+", "")))
                        .map(user -> RegisterCustomerResponse.builder()
                                .userId(user.getId())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .email(user.getEmail())
                                .roles(user.getRoles()
                                                .stream()
                                                .map(Role::getName)
                                                .collect(Collectors.toSet()))
                                .status(user.getStatus())
                                .createdAt(user.getCreatedAt())
                                .build()
                        ).orElseThrow(() -> new RuntimeException("User not found"))
        );
    }
}
