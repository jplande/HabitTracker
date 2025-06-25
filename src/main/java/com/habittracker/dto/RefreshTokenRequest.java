package com.habittracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class RefreshTokenRequest {

    @NotBlank(message = "Le refresh token est obligatoire")
    String refreshToken;
}