package com.manulife.studentportal.teacher.web;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignSubjectsRequest {

    @NotNull(message = "Subject IDs are required")
    private List<Long> subjectIds;
}
