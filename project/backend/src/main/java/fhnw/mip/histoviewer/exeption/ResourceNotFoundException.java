package fhnw.mip.histoviewer.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException{
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * @param message The detail message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}