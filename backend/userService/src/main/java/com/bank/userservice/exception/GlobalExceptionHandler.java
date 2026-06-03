package com.bank.userservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LogManager.getLogger();


//    public static final String NOT_FOUND = "404 Not Found";
//    public static final String BAD_REQUEST = "400 Bad Request";
//    public static final String CONFLICT = "409 Conflict";
//    public static final String UNPROCESSABLE_ENTITY = "422 Unprocessable Entity";
//    public static final String SERVER_ERROR = "500 An unexpected error occurred.";
//    public static final String SERVICE_UNAVAILABLE = "503 Service Unavailable";

    /**
     * @param error the type of error thrown
     * @param message the message of the error thrown
     * @return current time, errors status, errors label, errors message as a ErrorResponse object
     */
    ErrorResponse returnError(HttpStatus error, String message){
        return new ErrorResponse(
                LocalDateTime.now(),
                error.value(),
                error.getReasonPhrase(),
                message
        );
    }

    /**
     * @param exception response thrown
     * @return returnError with NOT_FOUND information
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<ErrorResponse> resourceNotFound(ResourceNotFoundException exception) {
        return new ResponseEntity<>(returnError(HttpStatus.NOT_FOUND, exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * @param exception response thrown
     * @return returnError with BAD_REQUEST information
     */
    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<ErrorResponse> badRequest(BadRequestException exception) {
        return new ResponseEntity<>(returnError(HttpStatus.BAD_REQUEST, exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * @param exception response thrown
     * @return returnError with CONFLICT information
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    protected ResponseEntity<ErrorResponse> emailAlreadyExists(EmailAlreadyExistsException exception) {
        return new ResponseEntity<>(returnError(HttpStatus.CONFLICT, exception.getMessage()), HttpStatus.CONFLICT);
    }

    /**
     * @param exception response thrown
     * @return returnError with INTERNAL_SERVER_ERROR information
     */
    @ExceptionHandler(ServerError.class)
    protected ResponseEntity<ErrorResponse> serverError(ServerError exception) {
        return new ResponseEntity<>(returnError(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param exception response thrown
     * @return returnError with UNAUTHORIZED information
     */
    @ExceptionHandler(UnauthorizedResponseException.class)
    protected ResponseEntity<ErrorResponse> unauthorizedResponse(UnauthorizedResponseException exception) {
        return new ResponseEntity<>(returnError(HttpStatus.UNAUTHORIZED, exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    /**
     * @param ex exception response.
     * @return the fields that caused the response as a string.
     */
    private String parseMessage(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        StringBuilder message = new StringBuilder();
        for (FieldError err : errors) {
            message.append(err.getDefaultMessage());
            message.append(" ");
        }
        return message.toString().trim();
    }
}
