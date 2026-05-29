package com.bank.userservice.controller;

import com.bank.userservice.dto.RegisterCustomerRequest;
import com.bank.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterCustomerRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}