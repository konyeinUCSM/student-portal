package com.manulife.studentportal.dto.request;

import java.util.List;

import jakarta.validation.Valid;
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
public class BatchMarkRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotNull(message = "Marks list is required")
    @Valid
    private List<BatchMarkEntry> marks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchMarkEntry {

        @NotNull(message = "Student ID is required")
        private Long studentId;

        @NotNull(message = "Score is required")
        @DecimalMin(value = "0.0", message = "Score must be at least 0.0")
        private Double score;

        @Size(max = 255, message = "Remarks must not exceed 255 characters")
        private String remarks;
    }
}
