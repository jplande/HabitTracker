package com.habittracker.dto.progress;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgressUpdateRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    @Positive(message = "La valeur doit être positive")
    Double value;

    @Size(max = 500, message = "La note ne peut pas dépasser 500 caractères")
    String note;

    public boolean hasChanges() {
        return date != null || value != null || note != null;
    }
}