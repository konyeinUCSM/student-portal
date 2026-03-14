package com.manulife.studentportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private Long totalUsers;

    private Long totalAdmins;

    private Long totalTeachers;

    private Long totalStudents;

    private Long totalClasses;

    private Long totalSubjects;

    private Long totalExams;

    private Long activeSessions;
}
