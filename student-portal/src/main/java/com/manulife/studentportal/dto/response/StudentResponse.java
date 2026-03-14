package com.manulife.studentportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long id;

    private String name;

    private String rollNumber;

    private String phone;

    private LocalDate dateOfBirth;

    private Long userId;

    private String username;

    private Long classId;

    private String className;

    private LocalDateTime createdAt;
}
