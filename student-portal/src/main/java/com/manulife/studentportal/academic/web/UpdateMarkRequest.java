package com.manulife.studentportal.academic.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMarkRequest {

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be at least 0.0")
    private Double score;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
