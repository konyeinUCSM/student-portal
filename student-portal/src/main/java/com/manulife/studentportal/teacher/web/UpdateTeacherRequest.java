package com.manulife.studentportal.teacher.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeacherRequest {

    private String name;

    private String phone;

    // staffId is NOT updatable
}
