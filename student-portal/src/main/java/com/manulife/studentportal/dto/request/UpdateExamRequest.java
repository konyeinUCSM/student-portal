package com.manulife.studentportal.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamRequest {

    private String name;

    private LocalDate examDate;

    @Min(value = 1, message = "Full marks must be at least 1")
    private Integer fullMarks;

    @Min(value = 0, message = "Pass marks must be at least 0")
    private Integer passMarks;

    // classId and subjectId are NOT updatable
}
