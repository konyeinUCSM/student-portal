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
public class UpdateSubjectRequest {

    @NotBlank(message = "Subject name is required")
    @Size(max = 50, message = "Subject name must not exceed 50 characters")
    private String name;
}
