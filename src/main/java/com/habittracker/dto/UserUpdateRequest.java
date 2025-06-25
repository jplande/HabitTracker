package com.habittracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateRequest {

    @Email(message = "L'email doit être valide")
    String email;

    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    String firstName;

    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    String lastName;

    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caractères")
    String password;

    public boolean isEmpty() {
        return email == null && firstName == null && lastName == null && password == null;
    }

    public boolean hasChanges() {
        return !isEmpty();
    }
}