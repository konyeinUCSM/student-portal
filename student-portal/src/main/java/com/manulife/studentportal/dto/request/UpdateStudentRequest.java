package com.manulife.studentportal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRequest {

    private String name;

    private String phone;

    private LocalDate dateOfBirth;

    private Long classId;

    // rollNumber is NOT updatable
}
