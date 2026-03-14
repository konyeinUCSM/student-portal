package com.manulife.studentportal.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {

    @NotBlank(message = "Exam name is required")
    private String name;

    private LocalDate examDate;

    @NotNull(message = "Full marks is required")
    @Min(value = 1, message = "Full marks must be at least 1")
    private Integer fullMarks;

    @NotNull(message = "Pass marks is required")
    @Min(value = 0, message = "Pass marks must be at least 0")
    private Integer passMarks;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
}
