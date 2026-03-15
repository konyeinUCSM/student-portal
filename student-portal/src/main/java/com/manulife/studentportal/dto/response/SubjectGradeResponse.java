package com.manulife.studentportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGradeResponse {

    private String subjectName;

    private Double averagePercentage;

    private String grade;

    private Integer examsCount;
}
