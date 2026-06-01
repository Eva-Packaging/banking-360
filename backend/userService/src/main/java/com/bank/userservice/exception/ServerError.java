package com.bank.userservice.exception;

import lombok.NoArgsConstructor;

// A custom exception for internal service errors.
@NoArgsConstructor
public class ServerError extends RuntimeException {
    // Throws a customized 500 error with the provided message.
    public ServerError(String message) { super(message); }
}