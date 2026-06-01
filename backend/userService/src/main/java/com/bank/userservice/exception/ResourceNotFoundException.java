package com.bank.userservice.exception;

import lombok.NoArgsConstructor;

// A custom exception format for 404 Resource Not Found errors.
@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException {
    // Throws a customized 404 error with the provided message.
    public ResourceNotFoundException(String message) { super(message); }
}