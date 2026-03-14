package com.manulife.studentportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
