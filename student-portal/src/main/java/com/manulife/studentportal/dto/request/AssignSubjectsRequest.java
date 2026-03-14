package com.manulife.studentportal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignSubjectsRequest {

    @NotNull(message = "Subject IDs are required")
    private List<Long> subjectIds;
}
