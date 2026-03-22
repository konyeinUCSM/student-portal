package com.manulife.studentportal.teacher.web;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {

    private Long id;

    private String name;

    private String staffId;

    private String phone;

    private Long userId;

    private String username;

    private List<String> classNames;

    private List<String> subjectNames;

    private LocalDateTime createdAt;
}
