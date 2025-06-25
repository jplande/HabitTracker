package com.habittracker.dto.progress;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Value
@Builder
@Jacksonized
public class ProgressCreateRequest {

    @NotNull(message = "La date est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    @NotNull(message = "La valeur est obligatoire")
    @Positive(message = "La valeur doit être positive")
    Double value;

    @Size(max = 500, message = "La note ne peut pas dépasser 500 caractères")
    String note;
}