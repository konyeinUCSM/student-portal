package com.manulife.studentportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkResponse {

    private Long id;

    private Long studentId;

    private String studentName;

    private Long examId;

    private String examName;

    private String subjectName;

    private String className;

    private Double score;

    private Integer fullMarks;

    private Double percentage;

    private String grade;

    private String remarks;

    private LocalDateTime createdAt;
}
