package com.bank.userservice.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception format for 409 Bad Request errors.
 */
@NoArgsConstructor
public class EmailAlreadyExistsException extends RuntimeException {
        // Throws a customized 409 error with the provided message.
        public EmailAlreadyExistsException(String message) { super(message); }
}