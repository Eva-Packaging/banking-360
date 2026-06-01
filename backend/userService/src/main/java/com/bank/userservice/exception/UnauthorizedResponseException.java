package com.bank.userservice.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// A custom exception for unauthorized response errors.
@NoArgsConstructor
public class UnauthorizedResponseException extends RuntimeException {
    // Throws a customized 401 error with the provided message.
    public UnauthorizedResponseException(String message) { super(message); }
}