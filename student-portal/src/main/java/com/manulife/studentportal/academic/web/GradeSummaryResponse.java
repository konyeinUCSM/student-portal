package com.manulife.studentportal.academic.web;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
