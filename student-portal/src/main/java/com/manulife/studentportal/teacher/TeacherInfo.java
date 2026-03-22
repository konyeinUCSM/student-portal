package com.manulife.studentportal.teacher;

import java.util.Set;

/**
 * Lightweight DTO for cross-module teacher data transfer.
 * Avoids exposing the Teacher entity directly.
 */
public record TeacherInfo(Long id, String name, Set<Long> classIds, Set<Long> subjectIds) {
}
