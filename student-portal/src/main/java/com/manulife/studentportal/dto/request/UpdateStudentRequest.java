package com.manulife.studentportal.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
