package com.manulife.studentportal.academic.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassRequest {

    @NotBlank(message = "Class name is required")
    @Size(max = 50, message = "Class name must not exceed 50 characters")
    private String name;
}
