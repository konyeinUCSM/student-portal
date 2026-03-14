package com.manulife.studentportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeSummaryResponse {

    private String studentName;

    private String className;

    private List<SubjectGradeResponse> subjectGrades;

    private Double overallPercentage;

    private String overallGrade;
}
