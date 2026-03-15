package com.manulife.studentportal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {

    private Long id;

    private String name;

    private LocalDate examDate;

    private Integer fullMarks;

    private Integer passMarks;

    private Long classId;

    private String className;

    private Long subjectId;

    private String subjectName;

    private LocalDateTime createdAt;
}
