package com.habittracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s non trouvé(e) avec %s : '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s non trouvé(e) avec l'ID : %d", resourceName, id));
    }
}