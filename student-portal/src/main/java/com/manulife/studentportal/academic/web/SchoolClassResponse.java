package com.manulife.studentportal.academic.web;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassResponse {

    private Long id;

    private String name;

    private int studentCount;

    private LocalDateTime createdAt;
}
