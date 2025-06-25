
// LoginRequest.java
package com.habittracker.dto.auth;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;

@Value
@Builder
@Jacksonized
public class LoginRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    String password;
}