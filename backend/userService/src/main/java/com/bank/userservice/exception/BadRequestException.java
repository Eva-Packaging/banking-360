package com.bank.userservice.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception format for 400 Bad Request errors.
 */
@NoArgsConstructor
public class BadRequestException extends RuntimeException {
     // Throws a customized 400 error with the provided message.
    public BadRequestException(String message) { super(message); }
}